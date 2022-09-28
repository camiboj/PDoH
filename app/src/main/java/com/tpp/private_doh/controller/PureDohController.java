package com.tpp.private_doh.controller;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.doh.DohResponse;
import com.tpp.private_doh.protocol.IpUtil;
import com.tpp.private_doh.util.DoHToDnsMapper;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class PureDohController implements Runnable {
    private static final String TAG = PureDohController.class.getSimpleName();
    ;
    private final DnsPacket dnsRequestPacket;
    private final BlockingQueue<DnsPacket> dnsResponsesQueue;
    private final DnsToDoHController dnsToDoHController;

    public PureDohController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue,
                             ShardingController shardingController) {
        this(dnsRequestPacket, dnsResponsesQueue, new DnsToDoHController(shardingController));
    }

    @VisibleForTesting
    public PureDohController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue,
                             DnsToDoHController dnsToDoHController) {
        this.dnsRequestPacket = dnsRequestPacket;
        this.dnsResponsesQueue = dnsResponsesQueue;
        this.dnsToDoHController = dnsToDoHController;
    }

    @Override
    public void run() {
        Log.i(TAG, "About to process a DNS Request");
        List<DohResponse> dohResponses = this.dnsToDoHController.process(dnsRequestPacket);
        List<DnsPacket> dnsResponsePackets = dohResponses.stream().map(
                this::createResponsePacket
        ).collect(Collectors.toList());

        dnsResponsePackets.forEach(this::offerPacket);
    }

    private DnsPacket createResponsePacket(DohResponse dohResponse) {
        DnsPacket dnsResponsePacket = IpUtil.buildDnsPacketFrom(dnsRequestPacket);
        DoHToDnsMapper.map(dohResponse, dnsResponsePacket);
        dnsResponsePacket.fillBackingBuffer();
        return dnsResponsePacket;
    }

    private void offerPacket(DnsPacket dnsResponsePacket) {
        dnsResponsesQueue.offer(dnsResponsePacket);
    }
}
