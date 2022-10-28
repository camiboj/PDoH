package com.tpp.private_doh.controller;

import com.tpp.private_doh.util.Requester;

import java.util.List;

public class HybridDnsShardingController extends ShardingController {
    private static final String TAG = DnsShardingController.class.getSimpleName();
    private final PingController pingController;

    public HybridDnsShardingController(PingController pingController) {
        super(pingController.getRequesters());
        this.pingController = pingController;
    }

    @Override
    protected List<Requester> getRequesters() {
        return pingController.getActiveRequesters(); // TODO: missing DoH. If it works is the same as DnsShardingController
    }
}
