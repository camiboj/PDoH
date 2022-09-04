package com.mocyx.basic_client.handler;

import android.net.VpnService;
import android.util.Log;

import com.mocyx.basic_client.protocol.IP4Header;
import com.mocyx.basic_client.protocol.IpUtil;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.protocol.UdpHeader;
import com.mocyx.basic_client.util.ByteBufferPool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

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

    private void hack (UdpTunnel tunnel, ByteBuffer byteBuffer, AtomicInteger ipId) {
        int headerSize = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE;
        byte[] data = new byte[byteBuffer.remaining()];
        int dataLen = Optional.ofNullable(data).map(dataAux -> dataAux.length).orElse(0);

        Packet packet = IpUtil.buildUdpPacket(tunnel.getRemote(), tunnel.getLocal(), ipId.addAndGet(1));

        packet.updateUDPBuffer(byteBuffer, dataLen);
        byteBuffer.position(headerSize + dataLen);
        ByteBuffer bufferDuplicated = byteBuffer.duplicate();
        bufferDuplicated.flip();
        this.networkToDeviceQueue.offer(byteBuffer);
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
                InetAddress destinationAddress = packet.getIp4Header().getDestinationAddress();
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
                    IP4Header ip4Header = packet.getIp4Header();

                    InetSocketAddress local = new InetSocketAddress(ip4Header.getSourceAddress(),
                            header.getSourcePort());
                    InetSocketAddress remote = new InetSocketAddress(ip4Header.getDestinationAddress(),
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