package com.mocyx.basic_client.dns;

import com.mocyx.basic_client.BitUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DnsQuestion {
    private String name;
    private final int type;
    private final int dnsQuestionClass;

    public DnsQuestion(ByteBuffer buffer) {
        // name, type, cd, ct, do, edns_client_subnet, random_padding
        this.name = buildName(buffer);
        this.type = BitUtils.getUnsignedShort(buffer.getShort());
        this.dnsQuestionClass = BitUtils.getUnsignedShort(buffer.getShort());
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
        return name;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DnsQuestion{");
        sb.append("name=").append(String.join(".", name));
        sb.append(", type=").append(type);
        sb.append(", dnsQuestionClass=").append(dnsQuestionClass);
        sb.append('}');
        return sb.toString();
    }
}
