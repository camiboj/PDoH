package com.tpp.private_doh.network;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.tpp.private_doh.app.MainActivity;
import com.tpp.private_doh.controller.PureDnsController;
import com.tpp.private_doh.controller.PureDohController;
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

    private FileChannel vpnInput;
    private FileChannel vpnOutput;
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
        FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
        FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();
        ExecutorService dnsWorkers = Executors.newFixedThreadPool(N_DNS_WORKERS);
        buildNetworkManager(vpnInput, vpnOutput, deviceToNetworkUDPQueue, deviceToNetworkTCPQueue,
                dnsResponsesQueue, networkToDeviceQueue, dnsWorkers);
    }

    @VisibleForTesting
    public NetworkManager(FileChannel vpnInput,
                          FileChannel vpnOutput,
                          BlockingQueue<Packet> deviceToNetworkUDPQueue,
                          BlockingQueue<Packet> deviceToNetworkTCPQueue,
                          BlockingQueue<DnsPacket> dnsResponsesQueue,
                          BlockingQueue<ByteBuffer> networkToDeviceQueue,
                          ExecutorService dnsWorkers) {
        buildNetworkManager(vpnInput, vpnOutput, deviceToNetworkUDPQueue, deviceToNetworkTCPQueue,
                dnsResponsesQueue, networkToDeviceQueue, dnsWorkers);
    }

    private void buildNetworkManager(FileChannel vpnInput,
                                     FileChannel vpnOutput,
                                     BlockingQueue<Packet> deviceToNetworkUDPQueue,
                                     BlockingQueue<Packet> deviceToNetworkTCPQueue,
                                     BlockingQueue<DnsPacket> dnsResponsesQueue,
                                     BlockingQueue<ByteBuffer> networkToDeviceQueue,
                                     ExecutorService dnsWorkers) {
        this.vpnInput = vpnInput;
        this.vpnOutput = vpnOutput;
        this.deviceToNetworkUDPQueue = deviceToNetworkUDPQueue;
        this.deviceToNetworkTCPQueue = deviceToNetworkTCPQueue;
        this.networkToDeviceQueue = networkToDeviceQueue;
        this.dnsResponsesQueue = dnsResponsesQueue;
        this.dnsWorkers = dnsWorkers;
    }

    @Override
    public void run() {
        Log.i(TAG, "Started");

        // Start thread that sends DNS responses back to the network
        Thread t = new Thread(new NetworkToDeviceManager(vpnOutput, networkToDeviceQueue));
        t.start();

        // Read incoming DNS, UDP and TCP requests and capture them
        try {
            while (!Thread.interrupted()) {
                ByteBuffer bufferToNetwork = ByteBufferPool.acquire();
                processPackets(bufferToNetwork);
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

    @VisibleForTesting
    public void processPackets(ByteBuffer bufferToNetwork) throws IOException {
        int readBytes = vpnInput.read(bufferToNetwork);
        MainActivity.upByte.addAndGet(readBytes);

        if (readBytes > 0) {
            bufferToNetwork.flip();
            Packet packet = PacketFactory.createPacket(bufferToNetwork);
            if (packet.isDNS()) {
                DnsPacket dnsPacket = (DnsPacket) packet;
                Log.i(TAG, String.format("[dns] This is a dns message: %s", dnsPacket));

                // TODO: create a more robust way to find out if we should bypass this packet
                if (dnsPacket.getLastQuestion().getName().equals("fiubaMap")) {
                    deviceToNetworkUDPQueue.offer(packet);
                } else {
                    //dnsWorkers.submit(new PureDohController(dnsPacket, dnsResponsesQueue));
                    dnsWorkers.submit(new PureDnsController(dnsPacket, dnsResponsesQueue));
                }

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
}

