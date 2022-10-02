package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.doh.Response;
import com.tpp.private_doh.protocol.IpUtil;
import com.tpp.private_doh.util.DoHToDnsMapper;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class NetworkController implements Runnable {
    private static final String TAG = NetworkController.class.getSimpleName();

    protected final DnsPacket dnsRequestPacket;
    protected final BlockingQueue<DnsPacket> dnsResponsesQueue;
    protected final DnsToController dnsToController;

    public NetworkController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue,
                             DnsToController dnsToController) {
        this.dnsRequestPacket = dnsRequestPacket;
        this.dnsResponsesQueue = dnsResponsesQueue;
        this.dnsToController = dnsToController;
    }

    public void run() {
        Log.i(TAG, "About to process a DNS Request");
        List<Response> responses = this.dnsToController.process(dnsRequestPacket);
        List<DnsPacket> dnsResponsePackets = responses.stream().map(
                this::createResponsePacket
        ).collect(Collectors.toList());

        dnsResponsePackets.forEach(this::offerPacket);
    }

    protected DnsPacket createResponsePacket(Response response) {
        DnsPacket dnsResponsePacket = IpUtil.buildDnsPacketFrom(dnsRequestPacket);
        DoHToDnsMapper.map(response, dnsResponsePacket);
        dnsResponsePacket.fillBackingBuffer();
        return dnsResponsePacket;
    }

    protected void offerPacket(DnsPacket dnsResponsePacket) {
        dnsResponsesQueue.offer(dnsResponsePacket);
    }
}
