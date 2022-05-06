package com.mocyx.basic_client;

import android.util.Log;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.doh.GoogleDohResponse;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.protocol.PacketFactory;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class DnsController implements Runnable{
    private static final String TAG = "DnsController";
    private final DnsPacket diveceOriginalDnsPacket;

    public DnsController(DnsPacket packet) {
        this.diveceOriginalDnsPacket = packet;
    }

    @Override
    public void run() {
        List<GoogleDohResponse> googleDohResponses = DnsToDoHController.process(diveceOriginalDnsPacket);
        List<DnsPacket> dnsPackets = googleDohResponses.stream().map(
                DoHToDnsController::process
        ).collect(Collectors.toList());

        dnsPackets.forEach(dnsPacket -> dnsPacket.copyHeaderFrom(diveceOriginalDnsPacket));

        dnsPackets.forEach(
                x-> Log.i(TAG, String.format("dnsPacket: %s", x))
        );

        ByteBuffer b = ByteBuffer.allocate(1000);
        List<ByteBuffer> byteBufferPackets = dnsPackets.stream().map(
                DnsController::fromPacketToBuffer
        ).collect(Collectors.toList());


        // THIS IS JUST TO TEST THAT THE BYTEBUFFER HAS THE CORRECT FORMAT
        Log.i(TAG, String.format("diveceOriginalDnsPacket: %s", diveceOriginalDnsPacket));
        List<Packet> recreatedPackets = byteBufferPackets.stream().map(
                DnsController::cratePacket
        ).collect(Collectors.toList());
    }

    private static Packet cratePacket(ByteBuffer b) {
        try {
            b.position(0);
            Packet packet = PacketFactory.createPacket(b);
            Log.i(TAG, String.format("cratePacket: %s", packet));
            return packet;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ByteBuffer fromPacketToBuffer(DnsPacket packet) {
        // TODO: allocate a logical size
        ByteBuffer b = ByteBuffer.allocate(1000);
        packet.putOn(b);
        return b;
    }
}
