package com.tpp.private_doh.protocol;

import com.tpp.private_doh.util.BitUtils;
import com.tpp.private_doh.util.IpUtils;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class IP6Header implements NetworkLayerHeader {
    public static final int IP6_HEADER_SIZE = 40;

    private final int versionTrafficClassFlowLabel;
    private short headerLength;
    private final int nextHeader;
    private final byte hopLimit;
    private final InetAddress sourceAddress;
    private final InetAddress destinationAddress;

    public IP6Header(int versionTrafficClassFlowLabel, short headerLength, byte nextHeader,
                     byte hopLimit, InetAddress sourceAddress, InetAddress destinationAddress) {
        this.versionTrafficClassFlowLabel = versionTrafficClassFlowLabel;
        this.headerLength = headerLength;
        this.nextHeader = nextHeader;
        this.hopLimit = hopLimit;
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
    }

    public IP6Header(ByteBuffer buffer) {
        this.versionTrafficClassFlowLabel = buffer.getInt();
        this.headerLength = buffer.getShort();
        this.nextHeader = BitUtils.getUnsignedByte(buffer.get());
        this.hopLimit = buffer.get();

        byte[] addressBytes = new byte[16];

        buffer.get(addressBytes, 0, 16);
        this.sourceAddress = IpUtils.getByAddress(addressBytes);

        buffer.get(addressBytes, 0, 16);
        this.destinationAddress = IpUtils.getByAddress(addressBytes);
    }

    public void fillBuffer(ByteBuffer buffer) {
        buffer.putInt(this.versionTrafficClassFlowLabel);
        buffer.putShort(this.headerLength);
        buffer.put((byte) this.nextHeader);
        buffer.put(this.hopLimit);
        buffer.put(sourceAddress.getAddress());
        buffer.put(destinationAddress.getAddress());
    }

    @Override
    public TransportProtocol getProtocol() {
        return TransportProtocol.numberToEnum(this.nextHeader);
    }

    @Override
    public int getHeaderSize() {
        return IP6_HEADER_SIZE;
    }

    @Override
    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    @Override
    public InetAddress getDestinationAddress() {
        return destinationAddress;
    }

    @Override
    public void setTotalLength(int length) {
        this.headerLength = (short) length;
    }

    @Override
    public int getHeaderLength() {
        return this.headerLength;
    }

    @Override
    public void setHeaderChecksum(int sum) {
        // Do nothing
    }

    @Override
    public boolean isIpv4() {
        return false;
    }

    @Override
    public void setIdentificationAndFlagsAndFragmentOffset(int i) {
        // Do nothing
    }
}
