package com.tpp.private_doh.controller;

import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.doh.Quad9DoHRequester;
import com.tpp.private_doh.util.Requester;

import java.util.Arrays;
import java.util.List;

public class ShardingControllerFactory {

    private final DnsShardingController pureDnsShardingController;
    private final ShardingController pureDohShardingController;
    //private ShardingController hybridDnsShardingController;

    public ShardingControllerFactory(PingController pingController, Integer racingAmount) {
        List<Requester> pureDohRequesters = Arrays.asList(new GoogleDoHRequester(), new CloudflareDoHRequester(), new Quad9DoHRequester());

        /*List<Requester> hybridDnsRequesters = new ArrayList<>();
        hybridDnsRequesters.addAll(pureDnsRequesters);
        hybridDnsRequesters.addAll(pureDohRequesters);*/

        this.pureDnsShardingController = new DnsShardingController(pingController);
        this.pureDohShardingController = new DohShardingController(pureDohRequesters, racingAmount);
        //this.hybridDnsShardingController = new ShardingController(hybridDnsRequesters, racingAmount);
    }

    public ShardingController getPureDnsShardingController() {
        return this.pureDnsShardingController;
    }

    public ShardingController getPureDohShardingController() {
        return this.pureDohShardingController;
    }

    /*public ShardingController getHybridDnsShardingController() {
        return this.hybridDnsShardingController;
    }*/
}
