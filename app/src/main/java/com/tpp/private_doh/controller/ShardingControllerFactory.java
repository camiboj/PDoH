package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.DoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.util.Requester;

import java.util.ArrayList;
import java.util.List;

public class ShardingControllerFactory {

    private final ShardingController pureDnsShardingController;
    private final ShardingController pureDohShardingController;
    private final ShardingController hybridDnsShardingController;

    public ShardingControllerFactory() {
        PublicDnsRequester publicDnsRequester = new PublicDnsRequester("8.8.8.8");
        PublicDnsRequester publicDnsRequester2 = new PublicDnsRequester("8.8.8.8");
        PublicDnsRequester publicDnsRequester3 = new PublicDnsRequester("8.8.8.8");

        DoHRequester googleDohRequester = new GoogleDoHRequester();
        DoHRequester cloudflareDohRequester = new CloudflareDoHRequester();
        DoHRequester quad9DohRequester = new CloudflareDoHRequester();

        List<Requester> pureDnsRequesters = new ArrayList<>();
        List<Requester> pureDohRequesters = new ArrayList<>();
        List<Requester> hybridDnsRequesters = new ArrayList<>();

        pureDnsRequesters.add(publicDnsRequester);
        pureDnsRequesters.add(publicDnsRequester2);
        pureDnsRequesters.add(publicDnsRequester3);

        pureDohRequesters.add(googleDohRequester);
        pureDohRequesters.add(cloudflareDohRequester);
        pureDohRequesters.add(quad9DohRequester);

        hybridDnsRequesters.add(publicDnsRequester);
        hybridDnsRequesters.add(googleDohRequester);
        hybridDnsRequesters.add(publicDnsRequester2);
        hybridDnsRequesters.add(cloudflareDohRequester);
        hybridDnsRequesters.add(publicDnsRequester3);
        hybridDnsRequesters.add(quad9DohRequester);

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
