package com.mocyx.basic_client;

import android.util.Log;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.doh.GoogleDohResponse;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class DnsController implements Runnable{
    private final DnsPacket packet;

    public DnsController(DnsPacket packet) {
        this.packet = packet;
    }

    @Override
    public void run() {
        List<GoogleDohResponse> googleDohResponses = DnsToDoHController.process(packet);
        List<DnsPacket> dnsPackets = googleDohResponses.stream().map(
                DoHToDnsController::process
        ).collect(Collectors.toList());

        ByteBuffer b = ByteBuffer.allocate(1000);
        dnsPackets.forEach(
                x-> Log.i("DnsController", String.format("dns packet: %s", x))
        );
        dnsPackets.forEach(
                packet -> packet.putOn(b)
        );
        Log.i("DnsController", String.format("byte buffer: %s", b));
    }
}
