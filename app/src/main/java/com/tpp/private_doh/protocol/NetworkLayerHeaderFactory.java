package com.tpp.private_doh.protocol;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class NetworkLayerHeaderFactory {
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

    public static NetworkLayerHeader createHeader(ByteBuffer buffer) {
        byte versionAndOtherField = buffer.get();
        buffer.position(0);
        byte version = (byte) (versionAndOtherField >> 4);
        if (version == (byte) 4) {
            return new IP4Header(buffer);
        }
        return new IP6Header(buffer);
    }

    public static NetworkLayerHeader createHeader(NetworkLayerHeader otherHeader) {
        if (otherHeader.isIpv4()) {
            IP4Header ip4Header = (IP4Header) otherHeader;

            InetAddress otherSourceAddress = ip4Header.getSourceAddress();
            InetAddress otherDestinationAddress = ip4Header.getDestinationAddress();
            int idAndFlagsAndFragmentOffset = ip4Header.getIdentificationAndFlagsAndFragmentOffset();

            return new IP4Header((byte) VERSION, (byte) IHL, UDP_HEADER_LENGTH,
                    TYPE_OF_SERVICE, TOTAL_LENGTH,
                    idAndFlagsAndFragmentOffset,
                    TTL, TransportProtocol.UDP.getNumber(), TransportProtocol.UDP, HEADER_CHECKSUM,
                    otherDestinationAddress, otherSourceAddress,
                    OPTIONS_AND_PADDING);
        }

        return null; // TODO: create ipv6 header
    }

    public static NetworkLayerHeader createHeader(int ipId, InetAddress sourceAddress,
                                                  InetAddress destinationAddress,
                                                  TransportProtocol transportProtocol) {
        return new IP4Header((byte) VERSION, (byte) IHL, IP4Header.IP4_HEADER_SIZE,
                TYPE_OF_SERVICE, TOTAL_LENGTH,
                ipId << 16 | IP_FLAG << 8 | IP_OFF,
                TTL, transportProtocol.getNumber(), transportProtocol, HEADER_CHECKSUM,
                sourceAddress, destinationAddress, OPTIONS_AND_PADDING);

        // TODO: add ipv6 management
    }
}