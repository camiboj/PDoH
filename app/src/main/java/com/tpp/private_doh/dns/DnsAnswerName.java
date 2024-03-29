package com.tpp.private_doh.dns;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class DnsAnswerName {
    public static final byte BEGINNING_MARK = (byte) 0xc0;
    private final List<String> name;

    public DnsAnswerName(String name) {
        this.name = Arrays.asList(name.split("\\.")); // This is not used because of firstAnswerPos usage
    }

    public void putOn(ByteBuffer buf, int firstAnswerNamePos) {
        buf.put(BEGINNING_MARK);
        buf.put((byte) firstAnswerNamePos);
    }
}

