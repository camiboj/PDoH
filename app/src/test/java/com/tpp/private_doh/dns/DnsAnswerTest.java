package com.tpp.private_doh.dns;

import static org.junit.Assert.assertEquals;

import com.tpp.private_doh.util.ByteBufferPool;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DnsAnswerTest {
    private DnsAnswer dnsAnswer;

    @Test
    public void testDnsAnswerType1BuiltOk() {
        String name = "dnsAnswerName";
        int type = 1;
        int ttl = 1;
        String data = "121.122.123.124";

        this.dnsAnswer = new DnsAnswer(name, type, ttl, data);
        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        int firstAnswerPos = 12;
        for (int i = 0; i < firstAnswerPos; i++) {
            byteBuffer.put((byte) 0); // Simulate header DNS
        }
        int otherAnswerPos = this.dnsAnswer.putOn(byteBuffer, firstAnswerPos);
        assertEquals(24, otherAnswerPos);
        assertEquals(28, byteBuffer.position());

        // Reset
        byteBuffer.position(firstAnswerPos);
        // Byte to indicate that it is a pointer
        assertEquals(-64, byteBuffer.get());
        // Byte to indicate name from request
        assertEquals(12, byteBuffer.get());
        // Bytes (2) to indicate type
        assertEquals(type, byteBuffer.getShort());
        // Bytes (2) to indicate class
        assertEquals(1, byteBuffer.getShort());
        // Bytes (4) to indicate ttl
        assertEquals(ttl, byteBuffer.getInt());
        // Bytes (2) to indicate that it is an ipv4
        assertEquals(4, byteBuffer.getShort());
        // Byte from ip
        assertEquals(121, byteBuffer.get());
        // Byte from ip
        assertEquals(122, byteBuffer.get());
        // Byte from ip
        assertEquals(123, byteBuffer.get());
        // Byte from ip
        assertEquals(124, byteBuffer.get());
        // The end
        assertEquals(0, byteBuffer.get());
    }

    @Test
    public void testDnsAnswerType5BuiltOk() {
        String name = "dnsAnswerName";
        int type = 5;
        int ttl = 1;
        String data = "com.other.cname";
        List<String> dataInList = new ArrayList<>();
        dataInList.add("com");
        dataInList.add("other");
        dataInList.add("cname");

        this.dnsAnswer = new DnsAnswer(name, type, ttl, data);
        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        int firstAnswerPos = 12;
        for (int i = 0; i < firstAnswerPos; i++) {
            byteBuffer.put((byte) 0); // Simulate header DNS
        }
        int otherAnswerPos = this.dnsAnswer.putOn(byteBuffer, firstAnswerPos);
        assertEquals(24, otherAnswerPos);

        assertEquals(41, byteBuffer.position());
        // Reset
        byteBuffer.position(firstAnswerPos);
        // Byte to indicate that it is a pointer
        assertEquals(-64, byteBuffer.get());
        // Byte to indicate name from request
        assertEquals(12, byteBuffer.get());
        // Bytes (2) to indicate type
        assertEquals(type, byteBuffer.getShort());
        // Bytes (2) to indicate class
        assertEquals(1, byteBuffer.getShort());
        // Bytes (4) to indicate ttl
        assertEquals(ttl, byteBuffer.getInt());
        // Bytes (2) to indicate that it is a CNAME
        assertEquals(data.length() + 1, byteBuffer.getShort());
        // Bytes for CNAME
        dataInList.forEach(element -> {
            assertEquals(element.length(), byteBuffer.get());
            byte[] elementBytes = element.getBytes();
            for (int i = 0; i < element.length(); i++) {
                assertEquals(elementBytes[i], byteBuffer.get());
            }
        });
        // The end
        assertEquals(0, byteBuffer.get());
    }

    @Test
    public void testDnsAnswerType5AfterType1BuiltOk() {
        String name = "dnsAnswerName";
        int type = 5;
        int ttl = 1;
        String data = "com.other.cname";
        List<String> dataInList = new ArrayList<>();
        dataInList.add("com");
        dataInList.add("other");
        dataInList.add("cname");

        this.dnsAnswer = new DnsAnswer(name, type, ttl, data);
        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        int firstAnswerPos = 12;
        for (int i = 0; i < firstAnswerPos; i++) {
            byteBuffer.put((byte) 0); // Simulate header DNS
        }
        int otherAnswerPos = this.dnsAnswer.putOn(byteBuffer, firstAnswerPos);
        assertEquals(24, otherAnswerPos);
        assertEquals(41, byteBuffer.position());

        String type1Data = "121.122.123.124";
        this.dnsAnswer = new DnsAnswer(name, 1, ttl, type1Data);
        int type1AnswerPos = this.dnsAnswer.putOn(byteBuffer, firstAnswerPos);
        assertEquals(41 + 12, type1AnswerPos);
        assertEquals(41 + 16, byteBuffer.position());
    }
}
