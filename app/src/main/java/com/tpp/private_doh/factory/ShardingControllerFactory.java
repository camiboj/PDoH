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
import com.tpp.private_doh.dns.RTT;
import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.doh.Quad9DoHRequester;
import com.tpp.private_doh.util.Requester;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ShardingControllerFactory {
    private static final Integer N_DOH_REQUESTERS = 3; // TODO if we add a new DohRequester we should change this number
    private final List<Requester> pureDohRequesters;
    private final String TAG = this.getClass().getSimpleName();
    private final ShardingController shardingController;
    private PingController pingController;

    public ShardingControllerFactory(ProtocolId protocolId, int racingAmount) {
        this.pureDohRequesters = Arrays.asList(new GoogleDoHRequester(), new CloudflareDoHRequester(), new Quad9DoHRequester());
        Log.i(TAG, "protocolId: " + protocolId);
        DohRequesterManager dohRequesterManager = new DohRequesterManager(pureDohRequesters, racingAmount);
        Thread pingControllerThread;

        switch (protocolId) {
            case DOH:
                Log.i(TAG, "DOH");
                this.shardingController = new DohShardingController(dohRequesterManager);
                break;
            case DNS:
                Log.i(TAG, "DNS");
                pingController = new PingController(racingAmount);
                pingControllerThread = new Thread(pingController);
                this.shardingController = new DnsShardingController(pingController);
                pingControllerThread.start();
                break;
            case HYBRID:
                Log.i(TAG, "BOTH");
                pingController = new PingController(racingAmount);
                pingControllerThread = new Thread(pingController);
                this.shardingController = new HybridDnsShardingController(pingController, dohRequesterManager);
                pingControllerThread.start();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + protocolId);
        }
    }

    public static int getAvailableRequesterAmount(ProtocolId protocolId) {
        switch (protocolId) {
            case DOH:
                Log.i("ShardingControllerFactory", "getAvailableRequesterAmount(DOH)");
                return N_DOH_REQUESTERS;
            case DNS:
                Log.i("ShardingControllerFactory", "getAvailableRequesterAmount(DNS)");
                return PublicDnsIps.RELIABLE_IPS.size();
            case HYBRID:
                Log.i("ShardingControllerFactory", "getAvailableRequesterAmount(BOTH)");
                return N_DOH_REQUESTERS + PublicDnsIps.RELIABLE_IPS.size();
            default:
                throw new IllegalStateException("Unexpected value: " + protocolId);
        }
    }

    public ShardingController getProtocolShardingController() {
        return this.shardingController;
    }

    public Map<String, Integer> getRequestersWinningMetrics() {
        return shardingController.getRequestersWinningMetrics();
    }

    public Map<String, RTT> getRequestersTimesMetrics() {
        return shardingController.getRequestersTimesMetrics();
    }

    public void destroy() {
        if (this.pingController != null) {
            this.pingController.stop();
        }
    }
}