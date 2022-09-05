package com.mocyx.basic_client.dns;

import java.nio.ByteBuffer;

public class DnsQuestionName extends DnsName {
    public DnsQuestionName(String name) {
        super(name);
    }

    public DnsQuestionName(ByteBuffer buffer) {
        super(buffer);
    }
}
