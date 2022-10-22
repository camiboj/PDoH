package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.util.Requester;

import java.util.List;
import java.util.stream.Collectors;

public class DnsShardingController extends ShardingController {
    private static final String TAG = DnsShardingController.class.getSimpleName();
    private final PingController pingController;

    public DnsShardingController(PingController pingController) {
        this.pingController = pingController;
    }

    @Override
    protected List<Requester> getRequesters() {
        return pingController.getActiveIps()
                .stream()
                .map(PublicDnsRequester::new)
                .collect(Collectors.toList());
    }
}
