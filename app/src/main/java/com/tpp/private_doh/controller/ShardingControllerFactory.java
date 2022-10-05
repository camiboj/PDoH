package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.DoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.util.Requester;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShardingControllerFactory {

    private final ShardingController pureDnsShardingController;
    private final ShardingController pureDohShardingController;
    private final ShardingController hybridDnsShardingController;

    public ShardingControllerFactory() {
        List<String> pureDnsResolvers = new ArrayList<>();
        pureDnsResolvers.add("208.67.222.222");
        pureDnsResolvers.add("208.67.220.220");
        pureDnsResolvers.add("1.1.1.1");
        pureDnsResolvers.add("1.0.0.1");
        pureDnsResolvers.add("8.8.8.8");
        pureDnsResolvers.add("8.8.4.4");
        pureDnsResolvers.add("9.9.9.9");
        pureDnsResolvers.add("149.112.112.112");

        DoHRequester googleDohRequester = new GoogleDoHRequester();
        DoHRequester cloudflareDohRequester = new CloudflareDoHRequester();
        DoHRequester quad9DohRequester = new CloudflareDoHRequester();

        List<Requester> pureDnsRequesters = pureDnsResolvers.stream().map(PublicDnsRequester::new).collect(Collectors.toList());
        List<Requester> pureDohRequesters = new ArrayList<>();
        List<Requester> hybridDnsRequesters = new ArrayList<>();

        pureDohRequesters.add(googleDohRequester);
        pureDohRequesters.add(cloudflareDohRequester);
        pureDohRequesters.add(quad9DohRequester);

        hybridDnsRequesters.addAll(pureDnsRequesters);
        hybridDnsRequesters.addAll(pureDohRequesters);

        this.pureDnsShardingController = new ShardingController(pureDnsRequesters, 2);
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
