package com.tpp.private_doh.dns;

import java.nio.ByteBuffer;

public class DnsQuestionName extends DnsName {
    public DnsQuestionName(String name) {
        super(name);
    }

    public DnsQuestionName(ByteBuffer buffer) {
        super(buffer);
    }
}
