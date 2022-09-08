package com.tpp.private_doh.network;

import android.util.Log;

import com.tpp.private_doh.app.MainActivity;
import com.tpp.private_doh.controller.DnsController;
import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.protocol.Packet;
import com.tpp.private_doh.protocol.PacketFactory;
import com.tpp.private_doh.util.ByteBufferPool;
import com.tpp.private_doh.util.ResourceUtils;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager implements Runnable {
    private static final String TAG = NetworkManager.class.getSimpleName();
    private static final int N_DNS_WORKERS = 50;

    private FileDescriptor vpnFileDescriptor;

    private BlockingQueue<Packet> deviceToNetworkUDPQueue;
    private BlockingQueue<Packet> deviceToNetworkTCPQueue;
    private BlockingQueue<DnsPacket> dnsResponsesQueue;
    private BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private ExecutorService dnsWorkers;

    public NetworkManager(FileDescriptor vpnFileDescriptor,
                          BlockingQueue<Packet> deviceToNetworkUDPQueue,
                          BlockingQueue<Packet> deviceToNetworkTCPQueue,
                          BlockingQueue<DnsPacket> dnsResponsesQueue,
                          BlockingQueue<ByteBuffer> networkToDeviceQueue) {
        this.vpnFileDescriptor = vpnFileDescriptor;
        this.deviceToNetworkUDPQueue = deviceToNetworkUDPQueue;
        this.deviceToNetworkTCPQueue = deviceToNetworkTCPQueue;
        this.networkToDeviceQueue = networkToDeviceQueue;
        this.dnsResponsesQueue = dnsResponsesQueue;

        dnsWorkers = Executors.newFixedThreadPool(N_DNS_WORKERS);
    }

    @Override
    public void run() {
        Log.i(TAG, "Started");
        FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();

        // Start thread that sends DNS responses back to the network
        FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();
        Thread t = new Thread(new NetworkToDeviceManager(vpnOutput, networkToDeviceQueue));
        t.start();

        // Read incoming DNS, UDP and TCP requests and capture them
        try {
            ByteBuffer bufferToNetwork;
            while (!Thread.interrupted()) {
                bufferToNetwork = ByteBufferPool.acquire();
                int readBytes = vpnInput.read(bufferToNetwork);

                MainActivity.upByte.addAndGet(readBytes);

                if (readBytes > 0) {
                    bufferToNetwork.flip();
                    Packet packet = PacketFactory.createPacket(bufferToNetwork);
                    if (packet.isDNS()) {
                        DnsPacket dnsPacket = (DnsPacket) packet;
                        Log.i(TAG, String.format("[dns] This is a dns message: %s", dnsPacket));
                        dnsWorkers.submit(new DnsController(dnsPacket, dnsResponsesQueue));
                    } else if (packet.isUDP()) {
                        deviceToNetworkUDPQueue.offer(packet);
                    } else if (packet.isTCP()) {
                        deviceToNetworkTCPQueue.offer(packet);
                    } else {
                        Log.w(TAG, String.format("Unknown packet protocol type %d",
                                packet.getIp4Header().getProtocol().getNumber()));
                    }
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            Log.w(TAG, e.toString(), e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ResourceUtils.closeResources(vpnInput, vpnOutput);
            dnsWorkers.shutdown();
        }
    }
}
