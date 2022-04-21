package com.mocyx.basic_client.protocol;

import com.mocyx.basic_client.BitUtils;
import com.mocyx.basic_client.udp.IP4Header;
import com.mocyx.basic_client.udp.TransportProtocol;
import com.mocyx.basic_client.udp.UDPHeader;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Representation of an IP Packet
 */
public class Packet {
    public static final int IP4_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 8;

    private IP4Header ip4Header;
    private UDPHeader udpHeader;
    private ByteBuffer backingBuffer;
    private boolean isUDP;

    public Packet(IP4Header ip4Header, UDPHeader udpHeader, ByteBuffer backingBuffer,
                  boolean isUDP) {
        this.ip4Header = ip4Header;
        this.udpHeader = udpHeader;
        this.backingBuffer = backingBuffer;
        this.isUDP = isUDP;
    }

    public Packet(ByteBuffer buffer) throws UnknownHostException {
        this.ip4Header = new IP4Header(buffer);
        if (ip4Header.getProtocol() == TransportProtocol.UDP) {
            this.udpHeader = new UDPHeader(buffer);
            this.isUDP = true;
        }
        this.backingBuffer = buffer;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Packet{");
        sb.append("ip4Header=").append(ip4Header);
        if (isUDP) sb.append(", udpHeader=").append(udpHeader);
        sb.append(", payloadSize=").append(backingBuffer.limit() - backingBuffer.position());
        sb.append('}');
        return sb.toString();
    }

    public boolean isDNS() {
        return this.isUDP() && this.udpHeader.getDestinationPort() == 53;
    }

    public boolean isUDP() {
        return isUDP;
    }

    public void updateUDPBuffer(ByteBuffer buffer, int payloadSize) {
        buffer.position(0);
        fillBuffer(buffer);
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

    private void fillBuffer(ByteBuffer buffer) {
        ip4Header.fillBuffer(buffer);
        if (isUDP) {
            udpHeader.fillBuffer(buffer);
        }
    }

    public IP4Header getIp4Header() {
        return ip4Header;
    }

    public UDPHeader getUdpHeader() {
        return udpHeader;
    }

    public ByteBuffer getBackingBuffer() {
        return backingBuffer;
    }
}

