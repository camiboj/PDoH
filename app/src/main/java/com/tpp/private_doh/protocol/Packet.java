package com.tpp.private_doh.protocol;


import com.tpp.private_doh.util.BitUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representation of an IP Packet
 */
public class Packet {
    public static final int TCP_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 8;

    private int packId;
    private boolean isTCP;
    private boolean isUDP;
    private NetworkLayerHeader networkLayerHeader;
    private TransportLayerHeader header;
    protected ByteBuffer backingBuffer;

    public Packet(NetworkLayerHeader networkLayerHeader, TransportLayerHeader header, ByteBuffer backingBuffer) {
        this.networkLayerHeader = networkLayerHeader;
        this.header = header;
        this.backingBuffer = backingBuffer;
        this.isTCP = true;
        this.isUDP = false;
        this.setPackId();
    }

    public Packet(NetworkLayerHeader networkLayerHeader, ByteBuffer backingBuffer) {
        this.networkLayerHeader = networkLayerHeader;
        this.backingBuffer = backingBuffer;
        this.isTCP = false;
        this.isUDP = false;
        this.setPackId();
    }

    public Packet(UdpHeader udpHeader, ByteBuffer backingBuffer) {
        this.header = udpHeader;
        this.isTCP = false;
        this.isUDP = true;
        this.backingBuffer = backingBuffer;
        this.setPackId();
    }

    private void setPackId() {
        AtomicInteger globalPackId = new AtomicInteger();
        this.packId = globalPackId.addAndGet(1);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Packet{");
        sb.append("ip4Header=").append(networkLayerHeader);
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
        return header != null && header.isTCP();
    }

    public boolean isUDP() {
        return header != null && header.isUDP();
    }

    public void updateTCPBuffer(ByteBuffer buffer, byte flags, long sequenceNum, long ackNum, int payloadSize) {
        buffer.position(0);
        fillHeader(buffer);
        backingBuffer = buffer;

        TcpHeader tcpHeader = (TcpHeader) header;

        tcpHeader.setFlags(flags);
        backingBuffer.put(this.networkLayerHeader.getHeaderSize() + 13, flags);

        tcpHeader.setSequenceNumber(sequenceNum);
        backingBuffer.putInt(this.networkLayerHeader.getHeaderSize() + 4, (int) sequenceNum);

        tcpHeader.setAcknowledgementNumber(ackNum);
        backingBuffer.putInt(this.networkLayerHeader.getHeaderSize() + 8, (int) ackNum);

        // Reset header size, since we don't need options
        byte dataOffset = (byte) (TCP_HEADER_SIZE << 2);
        tcpHeader.setDataOffsetAndReserved(dataOffset);
        backingBuffer.put(this.networkLayerHeader.getHeaderSize() + 12, dataOffset);

        updateTCPChecksum(payloadSize);

        int networkLayerHeaderTotalLength = this.networkLayerHeader.getHeaderSize() + TCP_HEADER_SIZE + payloadSize;
        backingBuffer.putShort(2, (short) networkLayerHeaderTotalLength);
        networkLayerHeader.setTotalLength(networkLayerHeaderTotalLength);

        updateIP4Checksum();
    }

    public void updateUDPBuffer(ByteBuffer buffer, int payloadSize) {
        buffer.position(0);
        fillHeader(buffer);
        backingBuffer = buffer;

        int udpTotalLength = UDP_HEADER_SIZE + payloadSize;
        backingBuffer.putShort(this.networkLayerHeader.getHeaderSize() + 4, (short) udpTotalLength);

        UdpHeader udpHeader = (UdpHeader) header;
        udpHeader.setLength(udpTotalLength);

        // Disable UDP checksum validation
        backingBuffer.putShort(this.networkLayerHeader.getHeaderSize() + 6, (short) 0);
        udpHeader.setChecksum(0);

        int ip4TotalLength = this.networkLayerHeader.getHeaderSize() + udpTotalLength;
        backingBuffer.putShort(2, (short) ip4TotalLength);
        networkLayerHeader.setTotalLength(ip4TotalLength);

        updateIP4Checksum();
    }

    public void updateIP4Checksum() {
        ByteBuffer buffer = backingBuffer.duplicate();
        buffer.position(0);

        // Clear previous checksum
        buffer.putShort(10, (short) 0);

        int ipLength = networkLayerHeader.getHeaderLength();
        int sum = 0;
        while (ipLength > 0) {
            sum += BitUtils.getUnsignedShort(buffer.getShort());
            ipLength -= 2;
        }
        while (sum >> 16 > 0)
            sum = (sum & 0xFFFF) + (sum >> 16);

        sum = ~sum;
        networkLayerHeader.setHeaderChecksum(sum);
        backingBuffer.putShort(10, (short) sum);
    }

    private void updateTCPChecksum(int payloadSize) {
        int sum;
        int tcpLength = TCP_HEADER_SIZE + payloadSize;

        // Calculate pseudo-header checksum
        ByteBuffer buffer = ByteBuffer.wrap(networkLayerHeader.getSourceAddress().getAddress());
        sum = BitUtils.getUnsignedShort(buffer.getShort()) + BitUtils.getUnsignedShort(buffer.getShort());

        buffer = ByteBuffer.wrap(networkLayerHeader.getDestinationAddress().getAddress());
        sum += BitUtils.getUnsignedShort(buffer.getShort()) + BitUtils.getUnsignedShort(buffer.getShort());

        sum += TransportProtocol.TCP.getNumber() + tcpLength;

        buffer = backingBuffer.duplicate();
        // Clear previous checksum
        buffer.putShort(this.networkLayerHeader.getHeaderSize() + 16, (short) 0);

        // Calculate TCP segment checksum
        buffer.position(this.networkLayerHeader.getHeaderSize());
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
        backingBuffer.putShort(this.networkLayerHeader.getHeaderSize() + 16, (short) sum);
    }

    protected void fillHeader(ByteBuffer buffer) {
        networkLayerHeader.fillBuffer(buffer);
        header.fillBuffer(buffer);
    }

    public NetworkLayerHeader getNetworkLayerHeader() {
        return networkLayerHeader;
    }

    public TransportLayerHeader getHeader() {
        return header;
    }

    public ByteBuffer getBackingBuffer() {
        return backingBuffer;
    }

    public int getNetworkLayerHeaderSize() {
        return this.networkLayerHeader.getHeaderSize();
    }

    public void setBackingBuffer(ByteBuffer byteBuffer) {
        this.backingBuffer = byteBuffer;
    }
}

