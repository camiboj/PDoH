package com.mocyx.basic_client;


import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Pair;

import com.mocyx.basic_client.config.Config;
import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.dns.DnsQuestion;
import com.mocyx.basic_client.handler.DnsDownWorker;
import com.mocyx.basic_client.handler.TcpPacketHandler;
import com.mocyx.basic_client.handler.UdpPacketHandler;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.protocol.PacketFactory;
import com.mocyx.basic_client.util.ByteBufferPool;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LocalVPNService extends VpnService {
    private static final String TAG = LocalVPNService.class.getSimpleName();
    private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything

    private ParcelFileDescriptor vpnInterface = null;

    private PendingIntent pendingIntent;

    private BlockingQueue<Packet> deviceToNetworkUDPQueue;
    private BlockingQueue<Packet> deviceToNetworkTCPQueue;
    private BlockingQueue<DnsPacket> dnsResponsesQueue;
    private BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private ExecutorService executorService;

    private static void closeResources(Closeable... resources) {
        for (Closeable resource : resources) {
            try {
                resource.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupVPN();
        deviceToNetworkUDPQueue = new ArrayBlockingQueue<>(1000);
        deviceToNetworkTCPQueue = new ArrayBlockingQueue<>(1000);
        dnsResponsesQueue = new ArrayBlockingQueue<>(1000);
        networkToDeviceQueue = new ArrayBlockingQueue<>(1000);

        executorService = Executors.newFixedThreadPool(4); // TODO: fix this, only for debugging
        executorService.submit(new UdpPacketHandler(deviceToNetworkUDPQueue, networkToDeviceQueue, this));
        executorService.submit(new TcpPacketHandler(deviceToNetworkTCPQueue, networkToDeviceQueue, this));
        executorService.submit(new DnsDownWorker(networkToDeviceQueue, dnsResponsesQueue));

        executorService.submit(new VPNRunnable(vpnInterface.getFileDescriptor(),
                deviceToNetworkUDPQueue, deviceToNetworkTCPQueue, dnsResponsesQueue,  networkToDeviceQueue));

        Log.i(TAG, "Started");
    }

    private void setupVPN() {
        try {
            if (vpnInterface == null) {
                Builder builder = new Builder();
                builder.addAddress(VPN_ADDRESS, 32);
                builder.addRoute(VPN_ROUTE, 0);
                builder.addDnsServer(Config.dns);
                if (Config.testLocal) {
                    builder.addAllowedApplication("com.mocyx.basic_client");
                }
                vpnInterface = builder.setSession(getString(R.string.app_name)).setConfigureIntent(pendingIntent).establish();
            }
        } catch (Exception e) {
            Log.e(TAG, "error", e);
            System.exit(0);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
        cleanup();
        Log.i(TAG, "Stopped");
    }

    private void cleanup() {
        deviceToNetworkTCPQueue = null;
        deviceToNetworkUDPQueue = null;
        networkToDeviceQueue = null;
        closeResources(vpnInterface);
    }

    private static class VPNRunnable implements Runnable {
        private static final String TAG = VPNRunnable.class.getSimpleName();
        private static final int N_DNS_WORKERS = 50;

        private FileDescriptor vpnFileDescriptor;

        private BlockingQueue<Packet> deviceToNetworkUDPQueue;
        private BlockingQueue<Packet> deviceToNetworkTCPQueue;
        private BlockingQueue<DnsPacket> dnsResponsesQueue;
        private BlockingQueue<ByteBuffer> networkToDeviceQueue;
        private ExecutorService dnsWorkers;

        public VPNRunnable(FileDescriptor vpnFileDescriptor,
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
            FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();
            Thread t = new Thread(new WriteVpnThread(vpnOutput, networkToDeviceQueue));
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
                        if (packet.isUDP()) {
                            Log.i(TAG, "read udp" + readBytes);
                            if (packet.isDNS()) {
                                DnsPacket dnsPacket = (DnsPacket) packet;
                                Log.i(TAG, String.format("[dns] this is a dns message: %s", dnsPacket));
                                //deviceToNetworkUDPQueue.offer(packet);
                                dnsWorkers.submit(new DnsController(dnsPacket, dnsResponsesQueue));
                            } else {
                                deviceToNetworkUDPQueue.offer(packet);
                            }
                        } else if (packet.isTCP()) {
                            Log.i(TAG, "read tcp " + readBytes);
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
                closeResources(vpnInput, vpnOutput);
                dnsWorkers.shutdown();
            }
        }

        static class WriteVpnThread implements Runnable {
            FileChannel vpnOutput;
            private BlockingQueue<ByteBuffer> networkToDeviceQueue;

            WriteVpnThread(FileChannel vpnOutput, BlockingQueue<ByteBuffer> networkToDeviceQueue) {
                this.vpnOutput = vpnOutput;
                this.networkToDeviceQueue = networkToDeviceQueue;
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        ByteBuffer bufferFromNetwork = networkToDeviceQueue.take();

                        bufferFromNetwork.flip();

                        // TODO: remove debugging tool
                        ByteBuffer packetBackingBuffer = bufferFromNetwork.duplicate();
                        Packet packet = PacketFactory.createPacket(packetBackingBuffer);
                        if (packet.isUDP()) {
                            if (packet.isDNS()) {
                                Log.i(TAG, "[input dns] this is a dns message");
                                Log.i(TAG, "[input dns] dns packet: " + packet);
                            }
                        }

                        while (bufferFromNetwork.hasRemaining()) {
                            int w = vpnOutput.write(bufferFromNetwork);
                            if (w > 0) {
                                MainActivity.downByte.addAndGet(w);
                            }
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "WriteVpnThread fail", e);
                    }
                }

            }
        }
    }
}

