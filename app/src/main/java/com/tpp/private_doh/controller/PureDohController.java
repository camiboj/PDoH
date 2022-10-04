package com.tpp.private_doh.controller;

import androidx.annotation.VisibleForTesting;

import com.tpp.private_doh.dns.DnsPacket;

import java.util.concurrent.BlockingQueue;

public class PureDohController extends NetworkController {
    private static final String TAG = PureDohController.class.getSimpleName();

    public PureDohController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue,
                             ShardingController pureDohShardingController) {
        this(dnsRequestPacket, dnsResponsesQueue, new DnsToDoHController(pureDohShardingController));
    }

    @VisibleForTesting
    public PureDohController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue,
                             DnsToController dnsToController) {
        super(dnsRequestPacket, dnsResponsesQueue, dnsToController);
    }
}
