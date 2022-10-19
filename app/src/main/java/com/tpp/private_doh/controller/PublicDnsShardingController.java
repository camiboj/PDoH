package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.util.Requester;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PublicDnsShardingController extends ShardingController {
    private static final String TAG = PublicDnsShardingController.class.getSimpleName();

    private PingController pingController;

    public PublicDnsShardingController(List<Requester> requesters, int n, PingController pingController) {
        super(requesters, n);
        this.pingController = pingController;
    }

    @Override
    public List<CompletableFuture<Response>> executeRequest(String name, int type) {
        List<Requester> requesters = this.requesters.get(actualIdx);
        this.actualIdx = ((actualIdx == (this.requesters.size() - 1)) ? 0 : actualIdx + 1);

        while (!allRequestersWork(requesters)) {
            Log.i(TAG, String.format("Trying to reach %s and %s", requesters.get(0).getIp(), requesters.get(1).getIp()));
            requesters = this.requesters.get(actualIdx);
            this.actualIdx = ((actualIdx == (this.requesters.size() - 1)) ? 0 : actualIdx + 1);
        }

        return requesters.stream()
                .map(requester -> requester.executeRequest(name, type))
                .collect(Collectors.toList());
    }

    private boolean allRequestersWork(List<Requester> requesters) {
        return requesters.stream().allMatch(requester -> this.pingController.isActive(requester.getIp()));
    }
}
