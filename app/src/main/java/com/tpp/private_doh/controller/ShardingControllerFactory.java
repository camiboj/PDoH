package com.tpp.private_doh.controller;

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

    private final ShardingController pureDnsShardingController;
    private ShardingController pureDohShardingController; // TODO: make final
    private ShardingController hybridDnsShardingController; // TODO: make final

    public ShardingControllerFactory(PingController pingController) {
        List<String> pureDnsResolvers = Arrays.asList("208.67.222.222", "208.67.220.220", "1.1.1.1", "1.0.0.1", "8.8.8.8", "8.8.4.4", "9.9.9.9", "149.112.112.112");
        List<Requester> pureDnsRequesters = pureDnsResolvers.stream().map(PublicDnsRequester::new).collect(Collectors.toList());

        List<Requester> pureDohRequesters = Arrays.asList(new GoogleDoHRequester(), new CloudflareDoHRequester(), new Quad9DoHRequester());

        List<Requester> hybridDnsRequesters = new ArrayList<>();
        hybridDnsRequesters.addAll(pureDnsRequesters);
        hybridDnsRequesters.addAll(pureDohRequesters);

        this.pureDnsShardingController = new ShardingController(pureDnsRequesters, 2, pingController); // TODO: only ping in this case
        /*this.pureDohShardingController = new ShardingController(pureDohRequesters, 2, pingController);
        this.hybridDnsShardingController = new ShardingController(hybridDnsRequesters, 2, pingController);*/
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
