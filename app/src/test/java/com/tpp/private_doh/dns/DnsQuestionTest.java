package com.tpp.private_doh.dns;

import static org.junit.Assert.assertEquals;

import com.tpp.private_doh.util.ByteBufferPool;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class DnsQuestionTest {
    private static String NAME = "com.test.name";
    private static short QUESTION_TYPE = 1;
    private static short QUESTION_CLASS = 1;
    private static List<String> NAME_SPLIT = Arrays.asList(NAME.split("\\."));

    @Test
    public void testBuildDnsQuestionWithByteBufferOk() {
        ByteBuffer buffer = buildByteBuffer();
        DnsQuestion dnsQuestion = new DnsQuestion(buffer);
        assertEquals(NAME, dnsQuestion.getName());
        assertEquals(QUESTION_TYPE, dnsQuestion.getType());
    }

    @Test
    public void testDnsQuestionPutsContentOnByteBufferOk() {
        DnsQuestion dnsQuestion = new DnsQuestion(NAME, QUESTION_TYPE);
        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        dnsQuestion.putOn(byteBuffer);
        byteBuffer.position(0);
        assertEquals(buildByteBuffer(), byteBuffer);
    }

    private ByteBuffer buildByteBuffer() {
        ByteBuffer buffer = ByteBufferPool.acquire();
        NAME_SPLIT.forEach(namePart -> {
            buffer.put((byte) namePart.length());
            buffer.put(namePart.getBytes());
        });
        buffer.put((byte) 0);
        buffer.putShort(QUESTION_TYPE);
        buffer.putShort(QUESTION_CLASS);
        buffer.position(0);
        return buffer;
    }
}
