package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.util.Requester;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DnsShardingController extends ShardingController {
    private static final String TAG = DnsShardingController.class.getSimpleName();
    private final PingController pingController;
    private int nSharders;

    public DnsShardingController(PingController pingController, int n) {
        this.pingController = pingController;
        this.nSharders = n;
    }

    @Override
    protected List<Requester> getRequesters() {
        return pingController.getRandomActiveIps(nSharders)
                .stream()
                .map(PublicDnsRequester::new)
                .collect(Collectors.toList());
    }
}
