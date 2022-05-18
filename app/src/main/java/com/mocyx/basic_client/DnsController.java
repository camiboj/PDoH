package com.mocyx.basic_client;

import android.util.Pair;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.doh.GoogleDohResponse;
import com.mocyx.basic_client.protocol.IpUtil;
import com.mocyx.basic_client.protocol.Packet;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

// TODO: add logs
public class DnsController implements Runnable {
    private static final String TAG = "DnsController";
    private final DnsPacket dnsRequestPacket;
    private final BlockingQueue<Packet> dnsResponsesQueue;

    public DnsController(DnsPacket dnsRequestPacket, BlockingQueue<Packet> dnsResponsesQueue) {
        this.dnsRequestPacket = dnsRequestPacket;
        this.dnsResponsesQueue = dnsResponsesQueue;
    }

    @Override
    public void run() {
        List<GoogleDohResponse> googleDohResponses = DnsToDoHController.process(dnsRequestPacket);
        List<DnsPacket> dnsResponsePackets = googleDohResponses.stream().map(
                this::createResponsePacket
        ).collect(Collectors.toList());

        dnsResponsePackets.forEach(this::offerPacket);

    }


    private DnsPacket createResponsePacket(GoogleDohResponse dohResponse) {
        DnsPacket dnsResponsePacket = IpUtil.buildDnsPacketFrom(dnsRequestPacket);
        DoHToDnsMapper.map(dohResponse, dnsResponsePacket);
        dnsResponsePacket.updateBackingBuffer();
        return dnsResponsePacket;
    }

    private void offerPacket(DnsPacket dnsResponsePacket) {
        dnsResponsesQueue.offer(dnsResponsePacket);
    }
}
