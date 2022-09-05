package com.tpp.private_doh.dns;

import java.nio.ByteBuffer;

public class DnsAnswerName extends DnsName {
    private final byte BEGINNING_MARK = (byte) 0xc0;

    public DnsAnswerName(String name) {
        super(name);
    }

    public void putOn(ByteBuffer buf, int firstAnswerNamePos) {
        buf.put(BEGINNING_MARK);
        buf.put((byte) firstAnswerNamePos);
    }
}

