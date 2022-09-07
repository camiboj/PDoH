package com.tpp.private_doh.dns;

import static org.junit.Assert.assertEquals;

import com.tpp.private_doh.util.ByteBufferPool;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class DnsNameTest {
    private static String NAME = "com.test.name";
    List<String> nameSplit;
    private ByteBuffer byteBuffer;

    @Before
    public void setUp() {
        nameSplit = Arrays.asList(NAME.split("\\."));

        // Build byteBuffer
        byteBuffer = ByteBufferPool.acquire();
        nameSplit.forEach(namePart -> {
            byteBuffer.put((byte) namePart.length());
            byteBuffer.put(namePart.getBytes());
        });
        byteBuffer.put((byte) 0);
        byteBuffer.position(0);
    }

    @Test
    public void testBuildDnsNameOk() {
        DnsName dnsName = new DnsName(byteBuffer);

        List<String> resultName = dnsName.getName();
        assertEquals(nameSplit, resultName);
    }

    @Test
    public void testPutNameInByteBufferWorks() {
        DnsName dnsName = new DnsName(NAME);

        List<String> resultName = dnsName.getName();
        assertEquals(nameSplit, resultName);

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        dnsName.putOn(byteBuffer);
        byteBuffer.position(0);
        assertEquals(this.byteBuffer, byteBuffer);
    }
}
