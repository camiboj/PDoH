package com.mocyx.basic_client;

import android.util.Log;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.protocol.PacketFactory;
import com.mocyx.basic_client.util.ByteBufferPool;
import com.mocyx.basic_client.util.ResourceUtils;

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
