package com.tpp.private_doh.handler;

import static com.tpp.private_doh.dns.DnsAnswer.ANSWER_CLASS;
import static com.tpp.private_doh.dns.DnsAnswerName.BEGINNING_MARK;
import static com.tpp.private_doh.dns.DnsQuestion.QUESTION_CLASS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.util.Helper;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.protocol.TransportProtocol;

import org.junit.Test;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DnsDownWorkerTest extends Helper {

    @Test
    public void testDnsDownWorkerHandlesPacketsOk() throws InterruptedException {
        BlockingQueue<ByteBuffer> networkToDeviceQueue = new ArrayBlockingQueue<>(1000);
        BlockingQueue<DnsPacket> dnsResponsesQueue = new ArrayBlockingQueue<>(1000);

        DnsPacket dnsPacket = buildDnsPacket();
        dnsPacket.fillBackingBuffer();
        dnsResponsesQueue.offer(dnsPacket);

        DnsDownWorker dnsDownWorker = new DnsDownWorker(networkToDeviceQueue, dnsResponsesQueue);
        dnsDownWorker.processPacket();
        assertEquals(1, networkToDeviceQueue.size());
        ByteBuffer buffer = networkToDeviceQueue.peek();
        assertNotNull(buffer);
        buffer.position(0);

        // Ip4Header
        assertEquals((byte) 69, buffer.get()); // Version + IHL
        assertEquals((byte) TYPE_OF_SERVICE, buffer.get());
        assertEquals(74, buffer.getShort());
        assertEquals(81920, buffer.getInt()); // Id and flags and fragment offset
        assertEquals((byte) TTL, buffer.get());
        assertEquals((byte) TransportProtocol.UDP.getNumber(), buffer.get());
        buffer.getShort(); // We don't care about checksum
        assertAddress(SOURCE_ADDRESS, buffer);
        assertAddress(DESTINATION_ADDRESS, buffer);

        // UdpHeader
        assertEquals(DNS_SOURCE_PORT, buffer.getShort());
        assertEquals(DESTINATION_PORT, buffer.getShort());
        assertEquals(54, buffer.getShort());
        buffer.getShort(); // We don't care about checksum

        // DnsHeader
        assertEquals(IDENTIFICATION, buffer.getShort());
        assertEquals(FLAGS, buffer.getShort());
        assertEquals(1, buffer.getShort());
        assertEquals(1, buffer.getShort());
        assertEquals(N_AUTHORITY_RESOURCE_RECORDS, buffer.getShort());
        assertEquals(N_ADDITIONAL_RRS, buffer.getShort());

        // DnsQuestion
        assertEquals((byte) QUESTION_NAME.length(), buffer.get());
        byte[] questionName = new byte[QUESTION_NAME.length()];
        buffer.get(questionName, 0, QUESTION_NAME.length());
        assertNBytes(QUESTION_NAME.getBytes(), questionName, QUESTION_NAME.length());
        assertEquals((byte) 0, buffer.get());
        assertEquals(QUESTION_TYPE, buffer.getShort());
        assertEquals(QUESTION_CLASS, buffer.getShort());

        // DnsAnswer
        assertEquals((byte) BEGINNING_MARK, buffer.get());
        assertEquals((byte) 12, buffer.get());
        assertEquals(ANSWER_TYPE, buffer.getShort());
        assertEquals(ANSWER_CLASS, buffer.getShort());
        assertEquals(TTL, buffer.getInt());
        assertEquals(4, buffer.getShort());
        assertEquals((byte) 129, buffer.get());
        assertEquals((byte) 130, buffer.get());
        assertEquals((byte) 131, buffer.get());
        assertEquals((byte) 132, buffer.get());
    }

    private void assertAddress(InetAddress address, ByteBuffer buffer) {
        int ipv4 = 4;
        byte[] addressBytes = new byte[ipv4];
        buffer.get(addressBytes, 0, ipv4);
        byte[] sourceAddressResult = address.getAddress();
        assertNBytes(addressBytes, sourceAddressResult, ipv4);
    }
}
