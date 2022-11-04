package com.tpp.private_doh.controller;

import com.tpp.private_doh.util.Requester;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HybridDnsShardingController extends ShardingController {
    private final PingController pingController;
    private final DohRequesterManager dohRequesterManager;
    private boolean isDns;

    public HybridDnsShardingController(PingController pingController,
                                       DohRequesterManager dohRequesterManager) {
        super(Stream.concat(pingController.getDnsRequesters().stream(),
                dohRequesterManager.getRequesters().stream()).collect(Collectors.toList()));
        this.pingController = pingController;
        this.isDns = false;
        this.dohRequesterManager = dohRequesterManager;
    }

    @Override
    protected List<Requester> getRequesters() {
        if (isDns) {
            isDns = false;
            return pingController.getActiveRequesters();
        }
        isDns = true;
        return dohRequesterManager.chooseRequesters();
    }
}
