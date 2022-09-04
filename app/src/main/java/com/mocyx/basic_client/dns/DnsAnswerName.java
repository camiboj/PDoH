package com.mocyx.basic_client.dns;

import java.nio.ByteBuffer;

public class DnsAnswerName extends DnsName {
    private final byte END_QUESTION_NAME_MARK = (byte) 0x0;
    private final byte BEGINNING_MARK = (byte) 0xc0;

    public DnsAnswerName(String name) {
        super(name);
    }

    public DnsAnswerName(ByteBuffer buffer) {
        super(buffer);
    }

    public void putOn(ByteBuffer buf, int firstAnswerNamePos) {
        buf.put(BEGINNING_MARK);
        buf.put((byte) firstAnswerNamePos);
    }
}

