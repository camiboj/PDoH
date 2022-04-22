package com.mocyx.basic_client.protocol;


import com.mocyx.basic_client.util.BitUtils;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representation of an IP Packet
 */
public class Packet {
    public static final int IP4_HEADER_SIZE = 20;
    public static final int TCP_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 8;

    private final com.mocyx.basic_client.protocol.IP4Header ip4Header;

    private int packId;
    private boolean isTCP;
    private boolean isUDP;
    private TcpHeader tcpHeader;
    private UdpHeader udpHeader;
    private ByteBuffer backingBuffer;

    // TODO: fix this with an interface

    public Packet(com.mocyx.basic_client.protocol.IP4Header ip4Header,
                  TcpHeader tcpHeader, ByteBuffer backingBuffer) {
        this.ip4Header = ip4Header;
        this.tcpHeader = tcpHeader;
        this.backingBuffer = backingBuffer;
        this.isTCP = true;
        this.isUDP = false;
        this.setPackId();
    }

    public Packet(com.mocyx.basic_client.protocol.IP4Header ip4Header,
                  UdpHeader udpHeader, ByteBuffer backingBuffer) {
        this.ip4Header = ip4Header;
        this.udpHeader = udpHeader;
        this.backingBuffer = backingBuffer;
        this.isTCP = false;
        this.isUDP = true;
        this.setPackId();
    }

    public Packet(ByteBuffer buffer) throws UnknownHostException {
        this.ip4Header = new com.mocyx.basic_client.protocol.IP4Header(buffer);
        if (this.ip4Header.getProtocol() == TransportProtocol.TCP) {
            this.tcpHeader = new TcpHeader(buffer);
            this.isTCP = true;
            this.isUDP = false;
        } else if (ip4Header.getProtocol() == TransportProtocol.UDP) {
            this.udpHeader = new UdpHeader(buffer);
            this.isUDP = true;
            this.isTCP = false;
        }
        this.backingBuffer = buffer;
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
        if (isTCP) sb.append(", tcpHeader=").append(tcpHeader);
        else if (isUDP) sb.append(", udpHeader=").append(udpHeader);
        sb.append(", payloadSize=").append(backingBuffer.limit() - backingBuffer.position());
        sb.append('}');
        return sb.toString();
    }

    public boolean isDNS() {
        return this.isUDP() && this.udpHeader.getDestinationPort() == 53;
    }

    public boolean isTCP() {
        return isTCP;
    }

    public boolean isUDP() {
        return isUDP;
    }

    public void updateTCPBuffer(ByteBuffer buffer, byte flags, long sequenceNum, long ackNum, int payloadSize) {
        buffer.position(0);
        fillHeader(buffer);
        backingBuffer = buffer;

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
        tcpHeader.setChecksum(sum);
        backingBuffer.putShort(IP4_HEADER_SIZE + 16, (short) sum);
    }

    private void fillHeader(ByteBuffer buffer) {
        ip4Header.fillBuffer(buffer);
        if (isUDP) {
            udpHeader.fillBuffer(buffer);
        } else if (isTCP) {
            tcpHeader.fillBuffer(buffer);
        }
    }

    public IP4Header getIp4Header() {
        return ip4Header;
    }

    public TcpHeader getTcpHeader() {
        return tcpHeader;
    }

    public ByteBuffer getBackingBuffer() {
        return backingBuffer;
    }

    public int getPackId() {
        return packId;
    }

    public UdpHeader getUdpHeader() {
        return udpHeader;
    }
}

