package com.tpp.private_doh.protocol;

import com.tpp.private_doh.dns.DnsHeader;
import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.util.ByteBufferPool;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class IpUtil {
    private static int VERSION = 4;
    private static int IHL = 5;
    private static int UDP_HEADER_LENGTH = 20;
    private static short TYPE_OF_SERVICE = 0;
    private static int TOTAL_LENGTH = 60;
    private static int IP_FLAG = 0x40;
    private static int IP_OFF = 0;
    private static short TTL = 64;
    private static int HEADER_CHECKSUM = 0;
    private static int OPTIONS_AND_PADDING = 0;
    private static int DATA_OFFSET_AND_RESERVED = -96;
    private static int TCP_HEADER_LENGTH = 40;
    private static int WINDOW = 65535;
    private static int URGENT_POINTER = 0;
    public static int FLAGS = 33152; // It was checked against a DNS packet

    public static DnsPacket buildDnsPacketFrom(DnsPacket other) {
        NetworkLayerHeader otherNetworkLayer = other.getNetworkLayerHeader();
        UdpHeader sourceUdpHeader = (UdpHeader) other.getHeader();
        DnsHeader sourceDnsHeader = other.getDnsHeader();

        int otherDestinationPort = sourceUdpHeader.getDestinationPort();
        int otherSourcePort = sourceUdpHeader.getSourcePort();

        NetworkLayerHeader networkLayerHeader = NetworkLayerHeaderFactory.createHeader(otherNetworkLayer);

        UdpHeader udpHeader = new UdpHeader(otherDestinationPort, otherSourcePort);
        DnsHeader dnsHeader = new DnsHeader(sourceDnsHeader.getIdentification(), FLAGS,
                0, 0, sourceDnsHeader.getNAuthorityResourceRecords(),
                sourceDnsHeader.getNAdditionalRRs());
        // We will add the nQuestions and nAnswers with the DoH response

        return new DnsPacket(networkLayerHeader, udpHeader, dnsHeader);
    }

    public static void updateIdentificationAndFlagsAndFragmentOffset(DnsPacket dnsResponse, int ipId) {
        dnsResponse.getNetworkLayerHeader().setIdentificationAndFlagsAndFragmentOffset(ipId << 16 | IP_FLAG << 8 | IP_OFF);
    }

    public static Packet buildUdpPacket(InetSocketAddress source, InetSocketAddress dest, int ipId,
                                        boolean isIpv4) {
        NetworkLayerHeader networkLayerHeader = NetworkLayerHeaderFactory.createHeader(ipId,
                source.getAddress(), dest.getAddress(), TransportProtocol.UDP, isIpv4);

        UdpHeader udpHeader = new UdpHeader(source.getPort(), dest.getPort());

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        byteBuffer.flip();

        return new Packet(networkLayerHeader, udpHeader, byteBuffer);
    }

    public static Packet buildTcpPacket(InetSocketAddress source, InetSocketAddress dest, byte flag,
                                        long ack, long seq, int ipId, boolean isIpv4) {
        NetworkLayerHeader networkLayerHeader = NetworkLayerHeaderFactory.createHeader(ipId,
                source.getAddress(), dest.getAddress(), TransportProtocol.TCP, isIpv4);

        TcpHeader tcpHeader = new TcpHeader(source.getPort(), dest.getPort(), seq,
                ack, (byte) DATA_OFFSET_AND_RESERVED, TCP_HEADER_LENGTH, flag,
                WINDOW, HEADER_CHECKSUM, URGENT_POINTER, null);

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        byteBuffer.flip();

        return new Packet(networkLayerHeader, tcpHeader, byteBuffer);
    }
}
