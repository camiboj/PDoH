package com.tpp.private_doh.controller;

import com.tpp.private_doh.constants.PublicDnsIps;
import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.util.Requester;

import java.util.List;
import java.util.stream.Collectors;

public class HybridDnsShardingController extends ShardingController {
    private static final String TAG = DnsShardingController.class.getSimpleName();
    private final PingController pingController;

    public HybridDnsShardingController(PingController pingController) {
        pingController.addDohRequesters();
        this.pingController = pingController;
    }

    @Override
    protected List<Requester> getRequesters() {
        return pingController.getActiveIps()
                .stream()
                .map(ip -> {
                    if (!PublicDnsIps.IPS.contains(ip)) {
                        return DohRequesterFactory.build(ip);
                    }
                    return new PublicDnsRequester(ip);
                })
                .collect(Collectors.toList());
    }
}
