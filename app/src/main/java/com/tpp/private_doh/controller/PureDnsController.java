package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.DnsPacket;

import java.util.concurrent.BlockingQueue;

public class PureDnsController extends NetworkController {

    public PureDnsController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue,
                             ShardingController shardingController) {
        super(dnsRequestPacket, dnsResponsesQueue, new DnsToPublicDnsController(shardingController));
    }
}
