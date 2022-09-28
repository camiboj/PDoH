package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.constants.PublicDnsIps;
import com.tpp.private_doh.protocol.Packet;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

public class PureDnsController implements Runnable {
    private static final String TAG = PureDnsController.class.getSimpleName();
    private final Packet dnsRequestPacket;
    private final BlockingQueue<Packet> deviceToNetworkUDPQueue;
    private final List<String> ips;
    private Message message;

    public PureDnsController(Packet dnsRequestPacket, BlockingQueue<Packet> deviceToNetworkUDPQueue) {
        this.dnsRequestPacket = dnsRequestPacket;
        this.deviceToNetworkUDPQueue = deviceToNetworkUDPQueue;
        this.ips = PublicDnsIps.IPS;
    }

    @Override
    public void run() {
        try {
            Record queryRecord = Record.newRecord(Name.fromString("play.googleapis.com."), Type.A, DClass.IN);
            Message queryMessage = Message.newQuery(queryRecord);
            Resolver r = new SimpleResolver("9.9.8.8");
            message = r.sendAsync(queryMessage)
                    .whenComplete(
                            (answer, ex) -> {
                                if (ex == null) {
                                    System.out.println(answer);
                                } else {
                                    ex.printStackTrace();
                                }
                            })
                    .toCompletableFuture()
                    .get();

            // TODO: read input of ips. Generify code
            // TODO: add some kind of mark to identify packets
        } catch (UnknownHostException | ExecutionException | InterruptedException | TextParseException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "[dns] About to send dnsPacket from PureDnsController");
        deviceToNetworkUDPQueue.offer(dnsRequestPacket);
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
