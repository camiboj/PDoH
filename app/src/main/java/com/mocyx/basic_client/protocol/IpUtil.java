package com.mocyx.basic_client.protocol;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.util.ByteBufferPool;

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

    public static DnsPacket buildDnsPacketFrom(DnsPacket other) {
        //InetAddress destinationAddress = other.getIp4Header().getSourceAddress();
        InetAddress sourceAddress = other.getIp4Header().getDestinationAddress();
        IP4Header ip4Header = new IP4Header((byte) VERSION, (byte) IHL, UDP_HEADER_LENGTH,
                TYPE_OF_SERVICE, TOTAL_LENGTH,
                0,
                TTL, TransportProtocol.UDP.getNumber(), TransportProtocol.UDP, HEADER_CHECKSUM,
                sourceAddress, sourceAddress,
                OPTIONS_AND_PADDING);


        int destinationPort = ((UdpHeader) other.getHeader()).getSourcePort();
        int sourcePort = ((UdpHeader) other.getHeader()).getDestinationPort();
        UdpHeader udpHeader = new UdpHeader(sourcePort, destinationPort);
        return new DnsPacket(ip4Header, udpHeader);
    }

    public static Packet buildUdpPacket(InetSocketAddress source, InetSocketAddress dest, int ipId) {
        IP4Header ip4Header = new IP4Header((byte) VERSION, (byte) IHL, UDP_HEADER_LENGTH,
                TYPE_OF_SERVICE, TOTAL_LENGTH,
                ipId << 16 | IP_FLAG << 8 | IP_OFF,
                TTL, TransportProtocol.UDP.getNumber(), TransportProtocol.UDP, HEADER_CHECKSUM,
                source.getAddress(), dest.getAddress(), OPTIONS_AND_PADDING);

        UdpHeader udpHeader = new UdpHeader(source.getPort(), dest.getPort());

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        byteBuffer.flip();

        return new Packet(ip4Header, udpHeader, byteBuffer);
    }

    public static Packet buildTcpPacket(InetSocketAddress source, InetSocketAddress dest, byte flag, long ack, long seq, int ipId) {
        IP4Header ip4Header = new IP4Header((byte) VERSION, (byte) IHL, UDP_HEADER_LENGTH,
                TYPE_OF_SERVICE, TOTAL_LENGTH,
                ipId << 16 | IP_FLAG << 8 | IP_OFF,
                TTL, TransportProtocol.TCP.getNumber(), TransportProtocol.TCP,
                HEADER_CHECKSUM, source.getAddress(), dest.getAddress(),
                OPTIONS_AND_PADDING);

        TcpHeader tcpHeader = new TcpHeader(source.getPort(), dest.getPort(), seq,
                ack, (byte) DATA_OFFSET_AND_RESERVED, TCP_HEADER_LENGTH, flag,
                WINDOW, HEADER_CHECKSUM, URGENT_POINTER, null);

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        byteBuffer.flip();

        return new Packet(ip4Header, tcpHeader, byteBuffer);
    }

    public static Packet buildUdpPacket(InetSocketAddress source, InetSocketAddress dest) {
        return buildUdpPacket(source, dest, 0);
    }
}
