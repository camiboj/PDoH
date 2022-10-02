package com.tpp.private_doh.controller;

import com.tpp.private_doh.constants.PublicDnsIps;
import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.dns.DnsRequester;
import com.tpp.private_doh.doh.Response;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class PureDnsController extends NetworkController {
    private static final String TAG = PureDnsController.class.getSimpleName();
    private final List<String> ips;
    private final DnsRequester dnsRequester;

    public PureDnsController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue) {
        super(dnsRequestPacket, dnsResponsesQueue);
        this.ips = PublicDnsIps.IPS;
        this.dnsRequester = new DnsRequester();
    }

    @Override
    public void run() {
        // TODO: move this to a DnsToDnsController
        List<DnsQuestion> questions = dnsRequestPacket.getQuestions();
        List<Response> responses = questions.stream().map(dnsQuestion -> {
            String name = dnsQuestion.getName() + "."; // This is a requirement of dns-java library
            return dnsRequester.executeRequest(name, dnsQuestion.getType());
        }).collect(Collectors.toList());

        List<DnsPacket> dnsResponsePackets = responses.stream().map(
                this::createResponsePacket
        ).collect(Collectors.toList());

        dnsResponsePackets.forEach(this::offerPacket);
    }
}
