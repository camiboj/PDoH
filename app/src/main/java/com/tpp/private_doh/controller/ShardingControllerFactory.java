package com.tpp.private_doh.controller;

import com.tpp.private_doh.constants.PublicDnsIps;
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
        List<Requester> pureDnsRequesters = PublicDnsIps.IPS.stream().map(PublicDnsRequester::new).collect(Collectors.toList());

        List<Requester> pureDohRequesters = Arrays.asList(new GoogleDoHRequester(), new CloudflareDoHRequester(), new Quad9DoHRequester());

        List<Requester> hybridDnsRequesters = new ArrayList<>();
        hybridDnsRequesters.addAll(pureDnsRequesters);
        hybridDnsRequesters.addAll(pureDohRequesters);

        this.pureDnsShardingController = new PublicDnsShardingController(pureDnsRequesters, 2, pingController); // TODO: only ping in this case
        this.pureDohShardingController = new ShardingController(pureDohRequesters, 2);
        this.hybridDnsShardingController = new ShardingController(hybridDnsRequesters, 2);
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
