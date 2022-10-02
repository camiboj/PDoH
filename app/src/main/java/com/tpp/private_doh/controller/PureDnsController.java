package com.tpp.private_doh.controller;

import com.tpp.private_doh.constants.PublicDnsIps;
import com.tpp.private_doh.dns.DnsPacket;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class PureDnsController extends NetworkController {
    private static final String TAG = PureDnsController.class.getSimpleName();
    private final List<String> ips;

    public PureDnsController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue,
                             DnsToController dnsToController) {
        super(dnsRequestPacket, dnsResponsesQueue, dnsToController);
        this.ips = PublicDnsIps.IPS;
    }
}
