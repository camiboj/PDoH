package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.mapper.DoHToDnsMapper;
import com.tpp.private_doh.protocol.IpUtil;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class DnsResponseProcessor implements Runnable {
    private static final String TAG = DnsResponseProcessor.class.getSimpleName();

    protected final DnsPacket dnsRequestPacket;
    protected final BlockingQueue<DnsPacket> dnsResponsesQueue;
    protected final DnsToController dnsToController;

    public DnsResponseProcessor(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue,
                                DnsToController dnsToController) {
        this.dnsRequestPacket = dnsRequestPacket;
        this.dnsResponsesQueue = dnsResponsesQueue;
        this.dnsToController = dnsToController;
    }

    public void run() {
        List<Response> responses = this.dnsToController.process(dnsRequestPacket);
        Log.i(TAG, "Obtaining responses");
        List<DnsPacket> dnsResponsePackets = responses.stream().map(
                this::createResponsePacket
        ).collect(Collectors.toList());

        dnsResponsePackets.forEach(this::offerPacket);
    }

    protected DnsPacket createResponsePacket(Response response) {
        Log.i(TAG, "About to create response packet");
        DnsPacket dnsResponsePacket = IpUtil.buildDnsPacketFrom(dnsRequestPacket);
        DoHToDnsMapper.map(response, dnsResponsePacket);
        dnsResponsePacket.fillBackingBuffer();
        return dnsResponsePacket;
    }

    protected void offerPacket(DnsPacket dnsResponsePacket) {
        Log.i(TAG, "About to send dns response to dns down worker");
        dnsResponsesQueue.offer(dnsResponsePacket);
    }
}
