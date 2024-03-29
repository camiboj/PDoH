package com.tpp.private_doh.network;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.tpp.private_doh.app.MainActivity;
import com.tpp.private_doh.config.Config;
import com.tpp.private_doh.controller.DnsResponseProcessor;
import com.tpp.private_doh.controller.DnsToController;
import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.factory.PacketFactory;
import com.tpp.private_doh.factory.ShardingControllerFactory;
import com.tpp.private_doh.protocol.Packet;
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

public class NetworkManager implements Runnable {
    private static final String TAG = NetworkManager.class.getSimpleName();
    private static ShardingControllerFactory shardingControllerFactory;

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
                          BlockingQueue<ByteBuffer> networkToDeviceQueue,
                          ExecutorService dnsWorkers) {
        FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
        FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();
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

    public static void setShardingControllerFactory(ShardingControllerFactory scd) {
        shardingControllerFactory = scd;
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
        } catch (Exception e) {
            Log.w(TAG, e.toString(), e);
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

                if (dnsPacket.getFirstQuestion().getName().equals(Config.PING_QUESTION)) {
                    deviceToNetworkUDPQueue.offer(packet);
                } else if (dnsPacket.getLastQuestion().getName().equals(Config.SENTINEL)) {
                    Log.i(TAG, "Reading sentinel");
                    deviceToNetworkUDPQueue.offer(packet);
                } else {
                    dnsWorkers.submit(new DnsResponseProcessor(dnsPacket, dnsResponsesQueue, new DnsToController(shardingControllerFactory.getProtocolShardingController())));
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
                Log.e(TAG, "The thread was interrupted");
            }
        }
    }

    private void destroy() {
        this.dnsWorkers.shutdown();
    }
}

