package com.mocyx.basic_client.protocol;


import com.mocyx.basic_client.util.BitUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representation of an IP Packet
 */
public class Packet {
    public static final int IP4_HEADER_SIZE = 20;
    public static final int TCP_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 8;

    private int packId;
    private boolean isTCP;
    private boolean isUDP;
    private IP4Header ip4Header;
    private Header header;
    private ByteBuffer backingBuffer;

    public Packet() {
    }

    public Packet(IP4Header ip4Header, Header header, ByteBuffer backingBuffer) {
        this.ip4Header = ip4Header;
        this.header = header;
        this.backingBuffer = backingBuffer;
        this.isTCP = true;
        this.isUDP = false;
        this.setPackId();
    }

    private void setPackId() {
        AtomicInteger globalPackId = new AtomicInteger();
        this.packId = globalPackId.addAndGet(1);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Packet{");
        sb.append("ip4Header=").append(ip4Header);
        if (isTCP) sb.append(", tcpHeader=").append(header);
        else if (isUDP) sb.append(", udpHeader=").append(header);
        sb.append(", payloadSize=").append(backingBuffer.limit() - backingBuffer.position());
        sb.append('}');
        return sb.toString();
    }

    public boolean isDNS() {
        return this.isUDP() && ((UdpHeader) this.header).isDNS();
    }

    public boolean isTCP() {
        return header.isTCP();
    }

    public boolean isUDP() {
        return header.isUDP();
    }

    public void updateTCPBuffer(ByteBuffer buffer, byte flags, long sequenceNum, long ackNum, int payloadSize) {
        buffer.position(0);
        fillHeader(buffer);
        backingBuffer = buffer;

        TcpHeader tcpHeader = (TcpHeader) header;

        tcpHeader.setFlags(flags);
        backingBuffer.put(IP4_HEADER_SIZE + 13, flags);

        tcpHeader.setSequenceNumber(sequenceNum);
        backingBuffer.putInt(IP4_HEADER_SIZE + 4, (int) sequenceNum);

        tcpHeader.setAcknowledgementNumber(ackNum);
        backingBuffer.putInt(IP4_HEADER_SIZE + 8, (int) ackNum);

        // Reset header size, since we don't need options
        byte dataOffset = (byte) (TCP_HEADER_SIZE << 2);
        tcpHeader.setDataOffsetAndReserved(dataOffset);
        backingBuffer.put(IP4_HEADER_SIZE + 12, dataOffset);

        updateTCPChecksum(payloadSize);

        int ip4TotalLength = IP4_HEADER_SIZE + TCP_HEADER_SIZE + payloadSize;
        backingBuffer.putShort(2, (short) ip4TotalLength);
        ip4Header.setTotalLength(ip4TotalLength);

        updateIP4Checksum();
    }

    public void updateUDPBuffer(ByteBuffer buffer, int payloadSize) {
        buffer.position(0);
        fillHeader(buffer);
        backingBuffer = buffer;

        int udpTotalLength = UDP_HEADER_SIZE + payloadSize;
        backingBuffer.putShort(IP4_HEADER_SIZE + 4, (short) udpTotalLength);

        UdpHeader udpHeader = (UdpHeader) header;
        udpHeader.setLength(udpTotalLength);

        // Disable UDP checksum validation
        backingBuffer.putShort(IP4_HEADER_SIZE + 6, (short) 0);
        udpHeader.setChecksum(0);

        int ip4TotalLength = IP4_HEADER_SIZE + udpTotalLength;
        backingBuffer.putShort(2, (short) ip4TotalLength);
        ip4Header.setTotalLength(ip4TotalLength);

        updateIP4Checksum();
    }

    private void updateIP4Checksum() {
        ByteBuffer buffer = backingBuffer.duplicate();
        buffer.position(0);

        // Clear previous checksum
        buffer.putShort(10, (short) 0);

        int ipLength = ip4Header.getHeaderLength();
        int sum = 0;
        while (ipLength > 0) {
            sum += BitUtils.getUnsignedShort(buffer.getShort());
            ipLength -= 2;
        }
        while (sum >> 16 > 0)
            sum = (sum & 0xFFFF) + (sum >> 16);

        sum = ~sum;
        ip4Header.setHeaderChecksum(sum);
        backingBuffer.putShort(10, (short) sum);
    }

    private void updateTCPChecksum(int payloadSize) {
        int sum;
        int tcpLength = TCP_HEADER_SIZE + payloadSize;

        // Calculate pseudo-header checksum
        ByteBuffer buffer = ByteBuffer.wrap(ip4Header.getSourceAddress().getAddress());
        sum = BitUtils.getUnsignedShort(buffer.getShort()) + BitUtils.getUnsignedShort(buffer.getShort());

        buffer = ByteBuffer.wrap(ip4Header.getDestinationAddress().getAddress());
        sum += BitUtils.getUnsignedShort(buffer.getShort()) + BitUtils.getUnsignedShort(buffer.getShort());

        sum += TransportProtocol.TCP.getNumber() + tcpLength;

        buffer = backingBuffer.duplicate();
        // Clear previous checksum
        buffer.putShort(IP4_HEADER_SIZE + 16, (short) 0);

        // Calculate TCP segment checksum
        buffer.position(IP4_HEADER_SIZE);
        while (tcpLength > 1) {
            sum += BitUtils.getUnsignedShort(buffer.getShort());
            tcpLength -= 2;
        }
        if (tcpLength > 0)
            sum += BitUtils.getUnsignedByte(buffer.get()) << 8;

        while (sum >> 16 > 0)
            sum = (sum & 0xFFFF) + (sum >> 16);

        sum = ~sum;
        TcpHeader tcpHeader = (TcpHeader) header;
        tcpHeader.setChecksum(sum);
        backingBuffer.putShort(IP4_HEADER_SIZE + 16, (short) sum);
    }

    private void fillHeader(ByteBuffer buffer) {
        ip4Header.fillBuffer(buffer);
        header.fillBuffer(buffer);
    }

    public IP4Header getIp4Header() {
        return ip4Header;
    }

    public Header getHeader() {
        return header;
    }

    public ByteBuffer getBackingBuffer() {
        return backingBuffer;
    }

    public int getPackId() {
        return packId;
    }
}

