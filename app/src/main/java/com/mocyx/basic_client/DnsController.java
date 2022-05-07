package com.mocyx.basic_client;

import android.util.Log;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.doh.GoogleDohResponse;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.protocol.PacketFactory;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class DnsController implements Runnable {
    private static final String TAG = "DnsController";
    private final DnsPacket dnsQuestionPacket;
    private final BlockingQueue<ByteBuffer> networkToDeviceQueue;

    public DnsController(DnsPacket packet, BlockingQueue<ByteBuffer> networkToDeviceQueue) {
        this.dnsQuestionPacket = packet;
        this.networkToDeviceQueue = networkToDeviceQueue;
    }

    @Override
    public void run() {
        List<GoogleDohResponse> googleDohResponses = DnsToDoHController.process(dnsQuestionPacket);
        List<DnsPacket> dnsResponsePackets = googleDohResponses.stream().map(
                DoHToDnsController::process
        ).collect(Collectors.toList());

        // TODO: check UdpDownWorker. maybe it is doing something similar
        dnsResponsePackets.forEach(dnsPacket -> dnsPacket.setResponseTo(dnsQuestionPacket));


        dnsResponsePackets.forEach(this::offerPacket);
    }


    private void offerPacket(DnsPacket packet) {
        // TEST TO SEE IF THE BYTE BUFFER (BACKING BUFFER) IS CORRECT
        Log.i(TAG, String.format("originalPacket: %s", packet));
        ByteBuffer b = packet.getBackingBuffer().duplicate();
        b.position(0);
        try {
            Packet newPacket = PacketFactory.createPacket(b);
            Log.i(TAG, String.format("newPacket: %s", newPacket));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // networkToDeviceQueue.offer(packet.getBackingBuffer());
    }
}
