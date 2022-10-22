package com.tpp.private_doh.factory;

import com.tpp.private_doh.controller.DnsShardingController;
import com.tpp.private_doh.controller.DohShardingController;
import com.tpp.private_doh.controller.HybridDnsShardingController;
import com.tpp.private_doh.controller.PingController;
import com.tpp.private_doh.controller.ShardingController;
import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.doh.Quad9DoHRequester;
import com.tpp.private_doh.util.Requester;

import java.util.Arrays;
import java.util.List;

public class ShardingControllerFactory {

    private final DnsShardingController pureDnsShardingController;
    private final ShardingController pureDohShardingController;
    private final PingController pingController;
    private ShardingController hybridDnsShardingController;

    public ShardingControllerFactory(PingController pingController, Integer racingAmount) {
        List<Requester> pureDohRequesters = Arrays.asList(new GoogleDoHRequester(), new CloudflareDoHRequester(), new Quad9DoHRequester());

        this.pingController = pingController;
        this.pureDnsShardingController = new DnsShardingController(pingController);
        this.pureDohShardingController = new DohShardingController(pureDohRequesters, racingAmount);
        this.hybridDnsShardingController = new HybridDnsShardingController(pingController);
    }

    public ShardingController getPureDnsShardingController() {
        return this.pureDnsShardingController;
    }

    public ShardingController getPureDohShardingController() {
        return this.pureDohShardingController;
    }

    public ShardingController getHybridDnsShardingController() {
        return this.hybridDnsShardingController;
    }
}
