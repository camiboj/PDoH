package com.mocyx.basic_client.dns;

import com.mocyx.basic_client.util.BitUtils;

import java.nio.ByteBuffer;

public class DnsQuestion {
    private final DnsQuestionName name;
    private final int type;
    private final int dnsQuestionClass;

    public DnsQuestion(ByteBuffer buffer) {
        this.name = new DnsQuestionName(buffer);
        this.type = BitUtils.getUnsignedShort(buffer.getShort());
        this.dnsQuestionClass = BitUtils.getUnsignedShort(buffer.getShort());
    }

    public DnsQuestion(String name, int type) {
        // name, type, cd, ct, do, dns_client_subnet, random_padding
        this.name = new DnsQuestionName(name);
        this.type = type;
        this.dnsQuestionClass = 1; // TODO: check default
    }

    private String buildName(ByteBuffer buffer) {
        final StringBuilder sb = new StringBuilder();
        short labelLength = BitUtils.getUnsignedByte(buffer.get());
        while (labelLength > 0) {
            for (int i = 0; i < labelLength; i++) { // TODO: investigate if its possible to get many chars at the same time
                char label = (char) buffer.get();
                sb.append(label);
            }
            sb.append(".");
            labelLength = BitUtils.getUnsignedByte(buffer.get());
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public String getName() {
        return name.join(".");
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "DnsQuestion{name=" + String.join(".", getName()) +
                ", type=" + type +
                ", dnsQuestionClass=" + dnsQuestionClass +
                '}';
    }

    public void putOn(ByteBuffer buff) {
        name.putOn(buff);
        buff.putShort(BitUtils.intToShort(type));
        buff.putShort(BitUtils.intToShort(dnsQuestionClass));
    }
}
