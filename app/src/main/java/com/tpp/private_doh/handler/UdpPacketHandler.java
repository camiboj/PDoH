package com.tpp.private_doh.handler;

import android.net.VpnService;
import android.util.Log;

import com.tpp.private_doh.protocol.IP4Header;
import com.tpp.private_doh.protocol.NetworkLayerHeader;
import com.tpp.private_doh.protocol.Packet;
import com.tpp.private_doh.protocol.UdpHeader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Class in charge of UDP packet handling
 */
public class UdpPacketHandler implements Runnable {
    private static final String TAG = UdpPacketHandler.class.getSimpleName();
    private static final Integer TUNNEL_CAPACITY = 100;
    private final BlockingQueue<Packet> queue;
    private final BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private final VpnService vpnService;
    private final Map<String, DatagramChannel> udpSockets;

    public UdpPacketHandler(BlockingQueue<Packet> queue, BlockingQueue<ByteBuffer> networkToDeviceQueue, VpnService vpnService) {
        this.queue = queue;
        this.networkToDeviceQueue = networkToDeviceQueue;
        this.vpnService = vpnService;
        this.udpSockets = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            BlockingQueue<UdpTunnel> tunnelQueue = new ArrayBlockingQueue<>(TUNNEL_CAPACITY);
            Selector selector = Selector.open();
            UdpDownWorker udpdw = new UdpDownWorker(selector, networkToDeviceQueue, tunnelQueue);
            Thread t = new Thread(udpdw);
            t.start();

            while (true) {
                Packet packet = queue.take();
                udpdw.setIsIpv4(packet.getNetworkLayerHeader().isIpv4());
                InetAddress destinationAddress = packet.getNetworkLayerHeader().getDestinationAddress();
                UdpHeader header = (UdpHeader) packet.getHeader();
                int destinationPort = header.getDestinationPort();
                int sourcePort = header.getSourcePort();

                String ipAndPort = destinationAddress.getHostAddress() + ":" + destinationPort + ":" + sourcePort;
                UdpTunnel tunnel;

                if (!udpSockets.containsKey(ipAndPort)) {
                    DatagramChannel outputChannel = DatagramChannel.open();
                    vpnService.protect(outputChannel.socket());

                    outputChannel.socket().bind(null);
                    outputChannel.connect(new InetSocketAddress(destinationAddress, destinationPort));
                    outputChannel.configureBlocking(false);
                    NetworkLayerHeader networkLayerHeader = packet.getNetworkLayerHeader();

                    InetSocketAddress local = new InetSocketAddress(networkLayerHeader.getSourceAddress(),
                            header.getSourcePort());
                    InetSocketAddress remote = new InetSocketAddress(networkLayerHeader.getDestinationAddress(),
                            header.getDestinationPort());
                    tunnel = new UdpTunnel(local, remote, outputChannel);
                    tunnelQueue.offer(tunnel);

                    selector.wakeup();

                    udpSockets.put(ipAndPort, outputChannel);
                }

                DatagramChannel outputChannel = udpSockets.get(ipAndPort);
                ByteBuffer buffer = packet.getBackingBuffer();
                try {
                    while (packet.getBackingBuffer().hasRemaining()) {
                        outputChannel.write(buffer);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Udp write error", e);
                    outputChannel.close();
                    udpSockets.remove(ipAndPort);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in BioUdpHandler", e);
            System.exit(0);
        }
    }
}