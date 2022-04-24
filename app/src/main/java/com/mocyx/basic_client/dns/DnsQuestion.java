package com.mocyx.basic_client.dns;

import com.mocyx.basic_client.BitUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
        // name, type, cd, ct, do, edns_client_subnet, random_padding
        this.name = new DnsQuestionName(name);
        this.type = type;
        this.dnsQuestionClass = 1; // TODO: check default
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
