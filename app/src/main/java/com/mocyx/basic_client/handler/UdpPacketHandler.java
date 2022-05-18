package com.mocyx.basic_client.handler;

import android.net.VpnService;
import android.util.Log;

import com.mocyx.basic_client.protocol.IP4Header;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.protocol.UdpHeader;

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

    @Override
    public void run() {
        try {
            BlockingQueue<UdpTunnel> tunnelQueue = new ArrayBlockingQueue<>(TUNNEL_CAPACITY);
            Selector selector = Selector.open();
            Thread t = new Thread(new UdpDownWorker(selector, networkToDeviceQueue, tunnelQueue));
            t.start();

            while (true) {
                Packet packet = queue.take();
                InetAddress destinationAddress = packet.getIp4Header().getDestinationAddress();
                UdpHeader header = (UdpHeader) packet.getHeader();
                int destinationPort = header.getDestinationPort();
                int sourcePort = header.getSourcePort();
                String ipAndPort = destinationAddress.getHostAddress() + ":" + destinationPort + ":" + sourcePort;

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
                    UdpTunnel tunnel = new UdpTunnel(local, remote, outputChannel);
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
/*
real: request
DnsHeader{identification=14156, flags=256, nQuestions=1, nAnswers=0, nAuthorityResourceRecords=0, nAdditionalRRs=0}
IP4Header{version=4, IHL=5, typeOfService=0, totalLength=72, identificationAndFlagsAndFragmentOffset=-446414848, TTL=64, protocol=17:UDP, headerChecksum=26202, sourceAddress=10.0.0.2, destinationAddress=114.114.114.114}UDPHeader{sourcePort=25339, destinationPort=53, length=52, checksum=6433}DnsPacket{header=DnsHeader{identification=14156, flags=256, nQuestions=1, nAnswers=0, nAuthorityResourceRecords=0, nAdditionalRRs=0}, questions=[DnsQuestion{name=pull-lls-l11.tiktokcdn.com, type=1, dnsQuestionClass=1}], answers=[]}
Buffer[pos=28 lim=72 cap=16384] = [69, 0, 0, 72, -27, 100, 64, 0, 64, 17, 102, 90, 10, 0, 0, 2, 114, 114, 114, 114, 98, -5, 0, 53, 0, 52, 25, 33, 55, 76, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 12, 112, 117, 108, 108, 45, 108, 108, 115, 45, 108, 49, 49, 9, 116, 105, 107, 116, 111, 107, 99, 100, 110, 3, 99, 111, 109, 0, 0, 1, 0, 1, 0, ...]
Output channel:
   . localAddress = /192.168.1.174:47735
   - remote addrs = /114.114.114.114:53


mio:
DnsHeader{identification=0, flags=0, nQuestions=1, nAnswers=1, nAuthorityResourceRecords=0, nAdditionalRRs=0}
IP4Header{version=4, IHL=5, typeOfService=0, totalLength=60, identificationAndFlagsAndFragmentOffset=0, TTL=64, protocol=17:UDP, headerChecksum=0, sourceAddress=192.168.1.1, destinationAddress=192.168.1.1}UDPHeader{sourcePort=53, destinationPort=34968, length=0, checksum=0}DnsPacket{header=DnsHeader{identification=0, flags=0, nQuestions=1, nAnswers=1, nAuthorityResourceRecords=0, nAdditionalRRs=0}, questions=[DnsQuestion{name=ar.amx.rcs.telephony.goog, type=35, dnsQuestionClass=1}], answers=[GoogleDohAnswer {name=Name {name=[ar, amx, rcs, telephony, goog]}, type=35, ttl=1, data=100 100 s SIPS+D2T  _sips._tcp.ar.amx.rcs.telephony.goog.}]}
Buffer[pos=28 lim=111 cap=16384] = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 2, 97, 114, 3, 97, 109, 120, 3, 114, 99, 115, 9, 116, 101, 108, 101, 112, 104, 111, 110, 121, 4, 103, 111, 111, 103, 0, 0, 35, 0, 1, 2, 97, 114, 3, 97, 109, 120, 3, 114, 99, 115, 9, 116, 101, 108, 101, 112, 104, 111, 110, 121, 4, 103, 111, 111, 103, 0, 0, 64, 72, 0, 35, 0, 1, 0, 0, 0, 1, 0, 57, 0, 0, ...]
Output channel:
   . localAddress = /192.168.1.174:47022 -> why is not 53!?
   - remote addrs = /192.168.1.1:34968
*/