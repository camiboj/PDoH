package com.tpp.private_doh.protocol;

import com.tpp.private_doh.dns.DnsPacket;

import java.nio.ByteBuffer;

public class PacketFactory {
    public static Packet createPacket(ByteBuffer buffer) {
        NetworkLayerHeader networkLayerHeader = NetworkLayerHeaderFactory.createHeader(buffer);
        if (networkLayerHeader.getProtocol() == TransportProtocol.TCP) {
            TcpHeader tcpHeader = new TcpHeader(buffer);
            return new Packet(networkLayerHeader, tcpHeader, buffer);
        } else if (networkLayerHeader.getProtocol() == TransportProtocol.UDP) {
            UdpHeader udpHeader = new UdpHeader(buffer);
            if (udpHeader.isDNS()) {
                return new DnsPacket(networkLayerHeader, udpHeader, buffer);
            }
            return new Packet(networkLayerHeader, udpHeader, buffer);
        }
        return new Packet(networkLayerHeader, buffer);
    }

    public static Packet createDnsPacket(ByteBuffer buffer) {
        NetworkLayerHeader networkLayerHeader = NetworkLayerHeaderFactory.createHeader(buffer);
        UdpHeader udpHeader = new UdpHeader(buffer);
        return new DnsPacket(networkLayerHeader, udpHeader, buffer);
    }
}


