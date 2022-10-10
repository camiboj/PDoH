package com.tpp.private_doh.protocol;

import com.tpp.private_doh.util.BitUtils;

import java.nio.ByteBuffer;

public class TcpHeader implements TransportLayerHeader {
    public static final int TCP_HEADER_SIZE = 20;
    public static final int FIN = 0x01;
    public static final int SYN = 0x02;
    public static final int RST = 0x04;
    public static final int PSH = 0x08;
    public static final int ACK = 0x10;
    public static final int URG = 0x20;

    private final int sourcePort;
    private final int destinationPort;
    private final int headerLength;
    private final int urgentPointer;
    private final int window;

    private long sequenceNumber;
    private long acknowledgementNumber;
    private byte dataOffsetAndReserved;
    private byte flags;
    private int checksum;
    private byte[] optionsAndPadding;

    public TcpHeader(int sourcePort, int destinationPort, long sequenceNumber,
                     long acknowledgementNumber, byte dataOffsetAndReserved,
                     int headerLength, byte flags, int window, int checksum,
                     int urgentPointer, byte[] optionsAndPadding) {
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.sequenceNumber = sequenceNumber;
        this.acknowledgementNumber = acknowledgementNumber;
        this.dataOffsetAndReserved = dataOffsetAndReserved;
        this.headerLength = headerLength;
        this.flags = flags;
        this.window = window;
        this.checksum = checksum;
        this.urgentPointer = urgentPointer;
        this.optionsAndPadding = optionsAndPadding;
    }

    public TcpHeader(ByteBuffer buffer) {
        this.sourcePort = BitUtils.getUnsignedShort(buffer.getShort());
        this.destinationPort = BitUtils.getUnsignedShort(buffer.getShort());
        this.sequenceNumber = BitUtils.getUnsignedInt(buffer.getInt());
        this.acknowledgementNumber = BitUtils.getUnsignedInt(buffer.getInt());
        this.dataOffsetAndReserved = buffer.get();
        this.headerLength = (this.dataOffsetAndReserved & 0xF0) >> 2;
        this.flags = buffer.get();
        this.window = BitUtils.getUnsignedShort(buffer.getShort());
        this.checksum = BitUtils.getUnsignedShort(buffer.getShort());
        this.urgentPointer = BitUtils.getUnsignedShort(buffer.getShort());

        int optionsLength = this.headerLength - TCP_HEADER_SIZE;
        if (optionsLength > 0) {
            optionsAndPadding = new byte[optionsLength];
            buffer.get(optionsAndPadding, 0, optionsLength);
        }
    }

    public boolean isFIN() {
        return (flags & FIN) == FIN;
    }

    public boolean isSYN() {
        return (flags & SYN) == SYN;
    }

    public boolean isRST() {
        return (flags & RST) == RST;
    }

    public boolean isPSH() {
        return (flags & PSH) == PSH;
    }

    public boolean isACK() {
        return (flags & ACK) == ACK;
    }

    public boolean isURG() {
        return (flags & URG) == URG;
    }

    @Override
    public void fillBuffer(ByteBuffer buffer) {
        buffer.putShort((short) sourcePort);
        buffer.putShort((short) destinationPort);

        buffer.putInt((int) sequenceNumber);
        buffer.putInt((int) acknowledgementNumber);

        buffer.put(dataOffsetAndReserved);
        buffer.put(flags);
        buffer.putShort((short) window);

        buffer.putShort((short) checksum);
        buffer.putShort((short) urgentPointer);
    }

    @Override
    public boolean isTCP() {
        return true;
    }

    @Override
    public boolean isUDP() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TCPHeader{");
        sb.append("sourcePort=").append(sourcePort);
        sb.append(", destinationPort=").append(destinationPort);
        sb.append(", sequenceNumber=").append(sequenceNumber);
        sb.append(", acknowledgementNumber=").append(acknowledgementNumber);
        sb.append(", headerLength=").append(headerLength);
        sb.append(", window=").append(window);
        sb.append(", checksum=").append(checksum);
        sb.append(", flags=");
        if (isFIN()) sb.append(" FIN");
        if (isSYN()) sb.append(" SYN");
        if (isRST()) sb.append(" RST");
        if (isPSH()) sb.append(" PSH");
        if (isACK()) sb.append(" ACK");
        if (isURG()) sb.append(" URG");
        sb.append('}');
        return sb.toString();
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public void setDataOffsetAndReserved(byte dataOffset) {
        this.dataOffsetAndReserved = dataOffset;
    }

    public void setChecksum(int sum) {
        this.checksum = sum;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNum) {
        this.sequenceNumber = sequenceNum;
    }

    public long getAcknowledgementNumber() {
        return acknowledgementNumber;
    }

    public void setAcknowledgementNumber(long ackNum) {
        this.acknowledgementNumber = ackNum;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }
}

