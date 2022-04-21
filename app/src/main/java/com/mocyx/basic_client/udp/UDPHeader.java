package com.mocyx.basic_client.udp;

import com.mocyx.basic_client.BitUtils;

import java.nio.ByteBuffer;

public class UDPHeader {
    private final int sourcePort;
    private final int destinationPort;
    private int length;
    private int checksum;

    public UDPHeader(ByteBuffer buffer) {
        this.sourcePort = BitUtils.getUnsignedShort(buffer.getShort());
        this.destinationPort = BitUtils.getUnsignedShort(buffer.getShort());
        this.length = BitUtils.getUnsignedShort(buffer.getShort());
        this.checksum = BitUtils.getUnsignedShort(buffer.getShort());
    }

    public UDPHeader(int sourcePort, int destinationPort) {
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.length = 0;
    }

    public void fillBuffer(ByteBuffer buffer) {
        buffer.putShort((short) this.sourcePort);
        buffer.putShort((short) this.destinationPort);
        buffer.putShort((short) this.length);
        buffer.putShort((short) this.checksum);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UDPHeader{");
        sb.append("sourcePort=").append(sourcePort);
        sb.append(", destinationPort=").append(destinationPort);
        sb.append(", length=").append(length);
        sb.append(", checksum=").append(checksum);
        sb.append('}');
        return sb.toString();
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public int getSourcePort() {
        return sourcePort;
    }
}
