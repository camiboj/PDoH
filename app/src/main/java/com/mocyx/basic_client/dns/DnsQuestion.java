package com.mocyx.basic_client.dns;

import com.mocyx.basic_client.BitUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DnsQuestion {
    private List<String> name;
    private final int type;
    private final int dnsQuestionClass;

    public DnsQuestion(ByteBuffer buffer) {
        this.name = buildName(buffer);
        this.type = BitUtils.getUnsignedShort(buffer.getShort());
        this.dnsQuestionClass = BitUtils.getUnsignedShort(buffer.getShort());
    }

    private List<String> buildName(ByteBuffer buffer) {
        List<String> name = new ArrayList<>();
        short labelLength = BitUtils.getUnsignedByte(buffer.get());
        while (labelLength > 0) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < labelLength; i++) { // TODO: investigate if its possible to get many chars at the same time
                char label = (char) buffer.get();
                sb.append(label);
            }
            name.add(sb.toString());
            labelLength = BitUtils.getUnsignedByte(buffer.get());
        }
        return name;
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
