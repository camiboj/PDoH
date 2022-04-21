package com.mocyx.basic_client.udp;

import com.mocyx.basic_client.BitUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IP4Header {
    private final byte version;
    private final byte IHL;
    private final int headerLength;
    private final short typeOfService;
    private final int identificationAndFlagsAndFragmentOffset;
    private final short TTL;
    private final short protocolNum;
    private final TransportProtocol protocol;
    private final InetAddress sourceAddress;
    private final InetAddress destinationAddress;
    private final int optionsAndPadding;

    private int totalLength;
    private int headerChecksum;

    public IP4Header(byte version, byte IHL, int headerLength, short typeOfService,
                     int totalLength, int identificationAndFlagsAndFragmentOffset,
                     short TTL, short protocolNum, TransportProtocol protocol,
                     int headerChecksum, InetAddress sourceAddress,
                     InetAddress destinationAddress, int optionsAndPadding) {
        this.version = version;
        this.IHL = IHL;
        this.headerLength = headerLength;
        this.typeOfService = typeOfService;
        this.totalLength = totalLength;
        this.identificationAndFlagsAndFragmentOffset = identificationAndFlagsAndFragmentOffset;
        this.TTL = TTL;
        this.protocolNum = protocolNum;
        this.protocol = protocol;
        this.headerChecksum = headerChecksum;
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.optionsAndPadding = optionsAndPadding;
    }

    public IP4Header(ByteBuffer buffer) throws UnknownHostException {
        byte versionAndIHL = buffer.get();
        this.version = (byte) (versionAndIHL >> 4);
        this.IHL = (byte) (versionAndIHL & 0x0F);
        this.headerLength = this.IHL << 2;

        this.typeOfService = BitUtils.getUnsignedByte(buffer.get());
        this.totalLength = BitUtils.getUnsignedShort(buffer.getShort());

        this.identificationAndFlagsAndFragmentOffset = buffer.getInt();

        this.TTL = BitUtils.getUnsignedByte(buffer.get());
        this.protocolNum = BitUtils.getUnsignedByte(buffer.get());
        this.protocol = TransportProtocol.numberToEnum(protocolNum);
        this.headerChecksum = BitUtils.getUnsignedShort(buffer.getShort());

        byte[] addressBytes = new byte[4];
        buffer.get(addressBytes, 0, 4);
        this.sourceAddress = InetAddress.getByAddress(addressBytes);

        buffer.get(addressBytes, 0, 4);
        this.destinationAddress = InetAddress.getByAddress(addressBytes);

        this.optionsAndPadding = 0;
    }

    public void fillBuffer(ByteBuffer buffer) {
        buffer.put((byte) (this.version << 4 | this.IHL));
        buffer.put((byte) this.typeOfService);
        buffer.putShort((short) this.totalLength);

        buffer.putInt(this.identificationAndFlagsAndFragmentOffset);

        buffer.put((byte) this.TTL);
        buffer.put((byte) this.protocol.getNumber());
        buffer.putShort((short) this.headerChecksum);

        buffer.put(this.sourceAddress.getAddress());
        buffer.put(this.destinationAddress.getAddress());
    }

    public TransportProtocol getProtocol() {
        return protocol;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IP4Header{");
        sb.append("version=").append(version);
        sb.append(", IHL=").append(IHL);
        sb.append(", typeOfService=").append(typeOfService);
        sb.append(", totalLength=").append(totalLength);
        sb.append(", identificationAndFlagsAndFragmentOffset=").append(identificationAndFlagsAndFragmentOffset);
        sb.append(", TTL=").append(TTL);
        sb.append(", protocol=").append(protocolNum).append(":").append(protocol);
        sb.append(", headerChecksum=").append(headerChecksum);
        sb.append(", sourceAddress=").append(sourceAddress.getHostAddress());
        sb.append(", destinationAddress=").append(destinationAddress.getHostAddress());
        sb.append('}');
        return sb.toString();
    }

    public void setTotalLength(int ip4TotalLength) {
        this.totalLength = ip4TotalLength;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public void setHeaderChecksum(int sum) {
        this.headerChecksum = sum;
    }

    public InetAddress getDestinationAddress() {
        return destinationAddress;
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }
}
