package com.tpp.private_doh.factory;

import android.util.Log;

import com.tpp.private_doh.controller.DnsShardingController;
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

public class ShardingControllerFactory {
    private final String TAG = this.getClass().getSimpleName();

    private final ShardingController shardingController;

    public ShardingControllerFactory(PingController pingController, Integer racingAmount,
                                     ProtocolId protocolId) {
        Log.i(TAG, "protocolId: " + protocolId);
        switch (protocolId) {
            case DOH:
                Log.i(TAG, "DOH");
                List<Requester> pureDohRequesters = Arrays.asList(new GoogleDoHRequester(), new CloudflareDoHRequester(), new Quad9DoHRequester());
                this.shardingController = new DohShardingController(pureDohRequesters, racingAmount);
                break;
            case DNS:
                Log.i(TAG, "DNS");
                this.shardingController = new DnsShardingController(pingController);
                break;
            case HYBRID:
                Log.i(TAG, "BOTH");
                this.shardingController = new HybridDnsShardingController(pingController);
                pingController.addDohRequesters();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + protocolId);
        }
    }

    public ShardingController getProtocolShardingController() {
        return this.shardingController;
    }
}
