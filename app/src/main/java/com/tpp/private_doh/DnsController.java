package com.tpp.private_doh;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.doh.GoogleDohResponse;
import com.tpp.private_doh.protocol.IpUtil;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class DnsController implements Runnable {
    private static final String TAG = DnsController.class.getSimpleName();
    ;
    private final DnsPacket dnsRequestPacket;
    private final BlockingQueue<DnsPacket> dnsResponsesQueue;

    public DnsController(DnsPacket dnsRequestPacket, BlockingQueue<DnsPacket> dnsResponsesQueue) {
        this.dnsRequestPacket = dnsRequestPacket;
        this.dnsResponsesQueue = dnsResponsesQueue;
    }

    @Override
    public void run() {
        Log.i(TAG, "About to process a DNS Request");
        List<GoogleDohResponse> googleDohResponses = DnsToDoHController.process(dnsRequestPacket);
        List<DnsPacket> dnsResponsePackets = googleDohResponses.stream().map(
                this::createResponsePacket
        ).collect(Collectors.toList());

        dnsResponsePackets.forEach(this::offerPacket);
    }

    private DnsPacket createResponsePacket(GoogleDohResponse dohResponse) {
        DnsPacket dnsResponsePacket = IpUtil.buildDnsPacketFrom(dnsRequestPacket);
        DoHToDnsMapper.map(dohResponse, dnsResponsePacket);
        dnsResponsePacket.fillBackingBuffer();
        return dnsResponsePacket;
    }

    private void offerPacket(DnsPacket dnsResponsePacket) {
        dnsResponsesQueue.offer(dnsResponsePacket);
    }
}
