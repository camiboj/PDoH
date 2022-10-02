package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.doh.Response;
import com.tpp.private_doh.protocol.IpUtil;
import com.tpp.private_doh.util.DoHToDnsMapper;

import java.util.concurrent.BlockingQueue;

public class NetworkController implements Runnable {
    protected final DnsPacket dnsRequestPacket;
    protected final BlockingQueue<DnsPacket> dnsResponsesQueue;

    public NetworkController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue) {
        this.dnsRequestPacket = dnsRequestPacket;
        this.dnsResponsesQueue = dnsResponsesQueue;
    }

    public void run() {
        throw new IllegalStateException("This should be implemented");
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
