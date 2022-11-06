package com.tpp.private_doh.factory;

import android.util.Log;

import com.tpp.private_doh.constants.PublicDnsIps;
import com.tpp.private_doh.controller.DnsShardingController;
import com.tpp.private_doh.controller.DohRequesterManager;
import com.tpp.private_doh.controller.DohShardingController;
import com.tpp.private_doh.controller.HybridDnsShardingController;
import com.tpp.private_doh.controller.PingController;
import com.tpp.private_doh.controller.ProtocolId;
import com.tpp.private_doh.controller.ShardingController;
import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.doh.Quad9DoHRequester;
import com.tpp.private_doh.util.Requester;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ShardingControllerFactory {
    private static final List<Requester> pureDohRequesters = Arrays.asList(new GoogleDoHRequester(), new CloudflareDoHRequester(), new Quad9DoHRequester());

    private final String TAG = this.getClass().getSimpleName();
    private final ShardingController shardingController;

    public ShardingControllerFactory(ProtocolId protocolId, int racingAmount) {
        PingController pingController = new PingController(racingAmount);
        Log.i(TAG, "protocolId: " + protocolId);
        // pureDohRequesters.forEach(dohRequester -> ((DoHRequester) dohRequester).restartCount());
        DohRequesterManager dohRequesterManager = new DohRequesterManager(pureDohRequesters, racingAmount);
        Thread t = new Thread(pingController);

        switch (protocolId) {
            case DOH:
                Log.i(TAG, "DOH");
                this.shardingController = new DohShardingController(dohRequesterManager);
                break;
            case DNS:
                Log.i(TAG, "DNS");
                this.shardingController = new DnsShardingController(pingController);
                t.start();
                break;
            case HYBRID:
                Log.i(TAG, "BOTH");
                this.shardingController = new HybridDnsShardingController(pingController, dohRequesterManager);
                t.start();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + protocolId);
        }
    }

    public static int getAvailableRequesterAmount(ProtocolId protocolId) {
        switch (protocolId) {
            case DOH:
                Log.i("ShardingControllerFactory", "getAvailableRequesterAmount(DOH)");
                return pureDohRequesters.size();
            case DNS:
                Log.i("ShardingControllerFactory", "getAvailableRequesterAmount(DNS)");
                return PublicDnsIps.RELIABLE_IPS.size();
            case HYBRID:
                Log.i("ShardingControllerFactory", "getAvailableRequesterAmount(BOTH)");
                return pureDohRequesters.size() + PublicDnsIps.RELIABLE_IPS.size();
            default:
                throw new IllegalStateException("Unexpected value: " + protocolId);
        }
    }

    public ShardingController getProtocolShardingController() {
        return this.shardingController;
    }

    public Map<String, Integer> getRequestersMetrics() {
        return shardingController.getRequestersMetrics();
    }
}