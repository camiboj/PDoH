package com.tpp.private_doh.controller;

import com.tpp.private_doh.util.Requester;

import java.util.List;

public class DnsShardingController extends ShardingController {
    private static final String TAG = DnsShardingController.class.getSimpleName();
    private final PingController pingController;

    public DnsShardingController(PingController pingController) {
        super(pingController.getDnsRequesters());
        this.pingController = pingController;
    }

    @Override
    protected List<Requester> getRequesters() {
        return pingController.getActiveRequesters();
    }
}
