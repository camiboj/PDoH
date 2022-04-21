package com.mocyx.basic_client.protocol;

import com.mocyx.basic_client.udp.IP4Header;
import com.mocyx.basic_client.udp.TransportProtocol;
import com.mocyx.basic_client.udp.UDPHeader;
import com.mocyx.basic_client.util.ByteBufferPool;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class UdpPacketBuilder {
    private static int VERSION = 4;
    private static int IHL = 5;
    private static int HEADER_LENGTH = 20;
    private static short TYPE_OF_SERVICE = 0;
    private static int TOTAL_LENGTH = 60;
    private static int IP_FLAG = 0x40;
    private static int IP_OFF = 0;
    private static short TTL = 64;
    private static short PROTOCOL_NUM = 17;
    private static int HEADER_CHECKSUM = 0;
    private static int OPTIONS_AND_PADDING = 0;

    public static Packet build(InetSocketAddress source, InetSocketAddress dest, int ipId) {
        IP4Header ip4Header = new IP4Header((byte) VERSION, (byte) IHL, HEADER_LENGTH,
                TYPE_OF_SERVICE, TOTAL_LENGTH,
                ipId << 16 | IP_FLAG << 8 | IP_OFF,
                TTL, PROTOCOL_NUM, TransportProtocol.UDP, HEADER_CHECKSUM,
                source.getAddress(), dest.getAddress(), OPTIONS_AND_PADDING);

        UDPHeader udpHeader = new UDPHeader(source.getPort(), dest.getPort());

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        byteBuffer.flip();

        return new Packet(ip4Header, udpHeader, byteBuffer, true);
    }
}
