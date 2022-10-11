package com.tpp.private_doh.protocol;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class NetworkLayerHeaderFactory {
    public static int VERSION_TRAFFIC_CLASS_FLOW_LABEL = 1610612736; // TODO: fix this
    public static int HOP_LIMIT = 0; // TODO: fix this
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
        IP4Header ip4Header = (IP4Header) otherHeader;

        InetAddress otherSourceAddress = ip4Header.getSourceAddress();
        InetAddress otherDestinationAddress = ip4Header.getDestinationAddress();
        int idAndFlagsAndFragmentOffset = ip4Header.getIdentificationAndFlagsAndFragmentOffset();

        if (otherHeader.isIpv4()) {
            return new IP4Header((byte) VERSION, (byte) IHL, UDP_HEADER_LENGTH,
                    TYPE_OF_SERVICE, TOTAL_LENGTH,
                    idAndFlagsAndFragmentOffset,
                    TTL, TransportProtocol.UDP.getNumber(), TransportProtocol.UDP, HEADER_CHECKSUM,
                    otherDestinationAddress, otherSourceAddress,
                    OPTIONS_AND_PADDING);
        }

        return new IP6Header(VERSION_TRAFFIC_CLASS_FLOW_LABEL, (short) IP6Header.IP6_HEADER_SIZE,
                (byte) TransportProtocol.UDP.getNumber(), (byte) HOP_LIMIT, otherDestinationAddress,
                otherSourceAddress);
    }


    public static NetworkLayerHeader createHeader(int ipId, InetAddress sourceAddress,
                                                  InetAddress destinationAddress,
                                                  TransportProtocol transportProtocol,
                                                  boolean isIpv4) {
        if (isIpv4) {
            return new IP4Header((byte) VERSION, (byte) IHL, IP4Header.IP4_HEADER_SIZE,
                    TYPE_OF_SERVICE, TOTAL_LENGTH,
                    ipId << 16 | IP_FLAG << 8 | IP_OFF,
                    TTL, transportProtocol.getNumber(), transportProtocol, HEADER_CHECKSUM,
                    sourceAddress, destinationAddress, OPTIONS_AND_PADDING);
        }

        return new IP6Header(VERSION_TRAFFIC_CLASS_FLOW_LABEL, (short) IP6Header.IP6_HEADER_SIZE,
                (byte) transportProtocol.getNumber(), (byte) HOP_LIMIT, sourceAddress, destinationAddress);
    }
}