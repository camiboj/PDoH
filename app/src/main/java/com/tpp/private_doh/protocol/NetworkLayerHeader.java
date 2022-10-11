package com.tpp.private_doh.protocol;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public interface NetworkLayerHeader {
    void fillBuffer(ByteBuffer buffer);
    TransportProtocol getProtocol();
    int getHeaderSize();
    InetAddress getSourceAddress();
    InetAddress getDestinationAddress();
    void setTotalLength(int length);
    int getHeaderLength();
    void setHeaderChecksum(int sum);
    boolean isIpv4();
    void setIdentificationAndFlagsAndFragmentOffset(int i);
}
