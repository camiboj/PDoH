package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.util.CombinationUtils;
import com.tpp.private_doh.util.Requester;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ShardingController {
    private static final String TAG = ShardingController.class.getSimpleName();
    private final PingController pingController;
    private int nSharders;

    public ShardingController(PingController pingController, int n) {
        this.pingController = pingController;
        this.nSharders = n;
    }

    public List<CompletableFuture<Response>> executeRequest(String name, int type) {
        List<Requester> requesters = pingController.getRandomActiveIps(nSharders)
                .stream()
                .map(PublicDnsRequester::new)
                .collect(Collectors.toList());

        return requesters.stream()
                .map(requester -> requester.executeRequest(name, type))
                .collect(Collectors.toList());
    }
}
