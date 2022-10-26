package com.tpp.private_doh.factory;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.protocol.IP4Header;
import com.tpp.private_doh.protocol.Packet;
import com.tpp.private_doh.protocol.TcpHeader;
import com.tpp.private_doh.protocol.TransportProtocol;
import com.tpp.private_doh.protocol.UdpHeader;

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
        }
        return new Packet(ip4Header, buffer);
    }

    public static Packet createDnsPacket(ByteBuffer buffer) throws UnknownHostException {
        IP4Header ip4Header = new IP4Header(buffer);
        UdpHeader udpHeader = new UdpHeader(buffer);
        return new DnsPacket(ip4Header, udpHeader, buffer);
    }
}


