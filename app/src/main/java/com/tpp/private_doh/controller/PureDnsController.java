package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.constants.PublicDnsIps;
import com.tpp.private_doh.dns.DnsRequester;
import com.tpp.private_doh.doh.Response;
import com.tpp.private_doh.protocol.Packet;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class PureDnsController implements Runnable {
    private static final String TAG = PureDnsController.class.getSimpleName();
    private final Packet dnsRequestPacket;
    private final BlockingQueue<Packet> deviceToNetworkUDPQueue;
    private final List<String> ips;
    private final DnsRequester dnsRequester;

    public PureDnsController(Packet dnsRequestPacket, BlockingQueue<Packet> deviceToNetworkUDPQueue) {
        this.dnsRequestPacket = dnsRequestPacket;
        this.deviceToNetworkUDPQueue = deviceToNetworkUDPQueue;
        this.ips = PublicDnsIps.IPS;
        this.dnsRequester = new DnsRequester();
    }

    @Override
    public void run() {
        Response response = dnsRequester.executeRequest("play.googleapis.com.", 1);
        Log.i(TAG, "[dns] About to send dnsPacket from PureDnsController");
        deviceToNetworkUDPQueue.offer(dnsRequestPacket); // TODO: remove this
    }

    /*
    @Override
    public void run() {
        Log.i(TAG, "1");
        byte[] address = dnsRequestPacket.getIp4Header().getDestinationAddress().getAddress();

        String pickedIp = "8.8.8.8";
        List<String> pickedIpSplitted = Arrays.asList(pickedIp.split("\\."));
        byte[] addressIp = new byte[4];
        for (int i = 0; i < 4; i++) {
            addressIp[i] = Byte.parseByte(pickedIpSplitted.get(i));
        }

        String pickedIp = this.ips.get(new Random().nextInt(this.ips.size()));

        boolean isReachable = IpUtils.isReachable(pickedIp);

        while(!isReachable) {
            pickedIp = this.ips.get(new Random().nextInt(this.ips.size()));
            isReachable = IpUtils.isReachable(pickedIp);
        }


        // Change destination address in both Ip4Header and backingBuffer
        Log.i(TAG, "2");
        dnsRequestPacket.getIp4Header().setDestinationAddress(addressIp);

        Log.i(TAG, "3");
        ByteBuffer backingBuffer = dnsRequestPacket.getBackingBuffer();
        int previousPos = backingBuffer.position();
        backingBuffer.position(16);
        pickedIpSplitted.forEach(ip -> backingBuffer.put(Byte.parseByte(ip)));
        backingBuffer.position(previousPos);
        dnsRequestPacket.updateIP4Checksum();

        Log.i(TAG, "[dns] About to send dnsPacket from PureDnsController");
        deviceToNetworkUDPQueue.offer(dnsRequestPacket);
    }*/
}
