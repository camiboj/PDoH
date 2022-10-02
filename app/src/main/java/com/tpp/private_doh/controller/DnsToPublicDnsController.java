package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.doh.Response;

import java.util.List;
import java.util.stream.Collectors;

public class DnsToPublicDnsController implements DnsToController {
    private final PublicDnsRequester publicDnsRequester;

    public DnsToPublicDnsController() {
        this.publicDnsRequester = new PublicDnsRequester();
    }

    public List<Response> process(DnsPacket dnsPacket) {
        List<DnsQuestion> questions = dnsPacket.getQuestions();
        return questions.stream().map(dnsQuestion -> {
            String name = dnsQuestion.getName() + "."; // This is a requirement of dns-java library
            return publicDnsRequester.executeRequest(name, dnsQuestion.getType());
        }).collect(Collectors.toList());
    }
}
