package com.tpp.private_doh.dns;

import static org.junit.Assert.assertEquals;

import com.tpp.private_doh.util.ByteBufferPool;

import org.junit.Test;

import java.nio.ByteBuffer;

public class DnsAnswerNameTest {
    @Test
    public void testAnswerNamePutsOk() {
        String name = "name";
        int firstAnswerPos = 12;

        DnsAnswerName dnsAnswerName = new DnsAnswerName(name);
        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        dnsAnswerName.putOn(byteBuffer, firstAnswerPos);
        assertEquals(2, byteBuffer.position());
        byteBuffer.position(0);
        assertEquals(-64, byteBuffer.get());
        assertEquals(firstAnswerPos, byteBuffer.get());
    }
}
