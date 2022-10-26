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
    private static ProtocolId PROTOCOL_ID;
    private static int RACING_AMOUNT;
    private static final List<String> pureDnsResolvers = Arrays.asList("208.67.222.222", "208.67.220.220", "1.1.1.1", "1.0.0.1", "8.8.8.8", "8.8.4.4", "9.9.9.9", "149.112.112.112");

    private final ShardingController protocolShardingController;
    private static final List<Requester> doHRequesters = Arrays.asList(new GoogleDoHRequester(), new CloudflareDoHRequester(), new Quad9DoHRequester());

    public ShardingControllerFactory() {
        List<Requester> requesters;
        Log.i(TAG, "protocolId: " + PROTOCOL_ID);
        switch (PROTOCOL_ID) {
            case DOH:
                Log.i(TAG, "DOH");
                requesters = doHRequesters;
                break;
            case DNS:
                Log.i(TAG, "DNS");
                requesters = pureDnsResolvers.stream().map(PublicDnsRequester::new).collect(Collectors.toList());
                break;
            case HYBRID:
                Log.i(TAG, "BOTH");
                requesters = new ArrayList<>();
                requesters.addAll(doHRequesters);
                requesters.addAll(pureDnsResolvers.stream().map(PublicDnsRequester::new).collect(Collectors.toList()));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + PROTOCOL_ID);
        }
        this.protocolShardingController = new ShardingController(requesters, RACING_AMOUNT);
    }

    public static void setProtocolId(ProtocolId n) {
        // must be call only once and before creating any instance of PDoHVpnService
        PROTOCOL_ID = n;
    }

    public static void setRacingAmount(int n) {
        // must be call only once and before creating any instance of PDoHVpnService
        RACING_AMOUNT = n;
    }

    public static int getAvailableRequesterAmount(ProtocolId protocolId) {
        switch (protocolId) {
            case DOH:
                Log.i("ShardingControllerFactory", "getAvailableRequesterAmount(DOH)");
                return doHRequesters.size();
            case DNS:
                Log.i("ShardingControllerFactory", "getAvailableRequesterAmount(DNS)");
                return pureDnsResolvers.size();
            case HYBRID:
                Log.i("ShardingControllerFactory", "getAvailableRequesterAmount(BOTH)");
                return doHRequesters.size() + pureDnsResolvers.size();
            default:
                throw new IllegalStateException("Unexpected value: " + protocolId);
        }
    }

    public ShardingController getProtocolShardingController() {
        return this.protocolShardingController;
    }
}
