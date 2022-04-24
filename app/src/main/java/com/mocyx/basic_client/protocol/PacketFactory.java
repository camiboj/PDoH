package com.mocyx.basic_client.protocol;

import com.mocyx.basic_client.dns.DnsPacket;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class PacketFactory {
    public static Packet createPacket(ByteBuffer buffer) throws UnknownHostException {
        IP4Header ip4Header = new IP4Header(buffer);
        if (ip4Header.getProtocol() == TransportProtocol.TCP) {
            TcpHeader tcpHeader = new TcpHeader(buffer);
            return new Packet(ip4Header, tcpHeader, buffer);
        } else if (ip4Header.getProtocol() == TransportProtocol.UDP) {
            UdpHeader udpHeader = new UdpHeader(buffer);
            if (udpHeader.isDNS()) {
                return new DnsPacket(ip4Header, udpHeader, buffer);
            }
            return new Packet(ip4Header, udpHeader, buffer);
        } else {
           return new Packet(ip4Header, buffer);
        }
    }
}
