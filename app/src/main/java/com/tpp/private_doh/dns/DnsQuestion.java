package com.tpp.private_doh.dns;

import com.tpp.private_doh.util.BitUtils;

import java.nio.ByteBuffer;

public class DnsQuestion {
    public static final int QUESTION_CLASS = 1;

    private final DnsName name;
    private final int type;
    private final int dnsQuestionClass;

    public DnsQuestion(ByteBuffer buffer) {
        this.name = new DnsName(buffer);
        this.type = BitUtils.getUnsignedShort(buffer.getShort());
        this.dnsQuestionClass = BitUtils.getUnsignedShort(buffer.getShort());
    }

    public DnsQuestion(String name, int type) {
        // name, type, cd, ct, do, dns_client_subnet, random_padding
        this.name = new DnsName(name);
        this.type = type;
        this.dnsQuestionClass = QUESTION_CLASS;
    }

    public String getName() {
        return name.join(".");
    }

    public int getType() {
        return type;
    }

    public void putOn(ByteBuffer buff) {
        name.putOn(buff);
        buff.putShort(BitUtils.intToShort(type));
        buff.putShort(BitUtils.intToShort(dnsQuestionClass));
    }

    @Override
    public String toString() {
        return "DnsQuestion{name=" + String.join(".", getName()) +
                ", type=" + type +
                ", dnsQuestionClass=" + dnsQuestionClass +
                '}';
    }
}
