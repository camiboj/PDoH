package com.mocyx.basic_client;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.doh.GoogleDohResponse;
import com.mocyx.basic_client.util.ByteBufferPool;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

// TODO: add logs
public class DnsController implements Runnable {
    private static final String TAG = "DnsController";
    private final DnsPacket dnsQuestionPacket;
    private final BlockingQueue<ByteBuffer> dnsResponsesQueue;

    public DnsController(DnsPacket packet, BlockingQueue<ByteBuffer> dnsResponsesQueue) {
        this.dnsQuestionPacket = packet;
        this.dnsResponsesQueue = dnsResponsesQueue;
    }

    @Override
    public void run() {
        List<GoogleDohResponse> googleDohResponses = DnsToDoHController.process(dnsQuestionPacket);
        List<DnsPacket> dnsResponsePackets = googleDohResponses.stream().map(
                DoHToDnsMapper::map
        ).collect(Collectors.toList());

        dnsResponsePackets.forEach(this::offerPacket);
    }


    private void offerPacket(DnsPacket packet) {
        ByteBuffer buffer = ByteBufferPool.acquire();
        packet.putOn(buffer);
        dnsResponsesQueue.offer(buffer);
    }
}
