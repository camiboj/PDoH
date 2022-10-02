package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.doh.Response;

import java.util.List;

public interface DnsToController {
    List<Response> process(DnsPacket dnsPacket);
}
