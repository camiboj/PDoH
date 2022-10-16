package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.doh.Quad9DoHRequester;
import com.tpp.private_doh.util.Requester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShardingControllerFactory {

    private final String TAG = this.getClass().getSimpleName();
    private final ShardingController protocolShardingController;

    public ShardingControllerFactory(int racingAmount, ProtocolId protocolId) {
        List<String> pureDnsResolvers = Arrays.asList("208.67.222.222", "208.67.220.220", "1.1.1.1", "1.0.0.1", "8.8.8.8", "8.8.4.4", "9.9.9.9", "149.112.112.112");
        List<Requester> requesters;
        Log.i(TAG, "protocolId: " + protocolId);
        switch (protocolId) {
            case DOH:
                Log.i(TAG, "DOH");
                requesters = Arrays.asList(new GoogleDoHRequester(), new CloudflareDoHRequester(), new Quad9DoHRequester());
                break;
            case DNS:
                Log.i(TAG, "DNS");
                requesters = pureDnsResolvers.stream().map(PublicDnsRequester::new).collect(Collectors.toList());
                break;
            case HYBRID:
                Log.i(TAG, "BOTH");
                requesters = new ArrayList<>();
                requesters.addAll(Arrays.asList(new GoogleDoHRequester(), new CloudflareDoHRequester(), new Quad9DoHRequester()));
                requesters.addAll(pureDnsResolvers.stream().map(PublicDnsRequester::new).collect(Collectors.toList()));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + protocolId);
        }

        this.protocolShardingController = new ShardingController(requesters, racingAmount);
    }

    public ShardingController getProtocolController() {
        return this.protocolShardingController;
    }
}
