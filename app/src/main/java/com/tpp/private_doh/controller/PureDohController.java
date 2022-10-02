package com.tpp.private_doh.controller;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.doh.Response;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class PureDohController extends NetworkController {
    private static final String TAG = PureDohController.class.getSimpleName();

    private final DnsToDoHController dnsToDoHController;

    public PureDohController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue,
                             ShardingController shardingController) {
        this(dnsRequestPacket, dnsResponsesQueue, new DnsToDoHController(shardingController));
    }

    @VisibleForTesting
    public PureDohController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue,
                             DnsToDoHController dnsToDoHController) {
        super(dnsRequestPacket, dnsResponsesQueue);
        this.dnsToDoHController = dnsToDoHController;
    }

    @Override
    public void run() {
        Log.i(TAG, "About to process a DNS Request");
        List<Response> responses = this.dnsToDoHController.process(dnsRequestPacket);
        List<DnsPacket> dnsResponsePackets = responses.stream().map(
                this::createResponsePacket
        ).collect(Collectors.toList());

        dnsResponsePackets.forEach(this::offerPacket);
    }
}
