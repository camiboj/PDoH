package com.tpp.private_doh.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.util.Helper;

import com.tpp.private_doh.dns.DnsHeader;
import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.protocol.IP4Header;
import com.tpp.private_doh.protocol.TransportProtocol;
import com.tpp.private_doh.protocol.UdpHeader;

import org.junit.Test;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DnsDownWorkerTest extends Helper {

    @Test
    public void testDnsDownWorkerHandlesPacketsOk() throws InterruptedException {
        BlockingQueue<ByteBuffer> networkToDeviceQueue = new ArrayBlockingQueue<>(1000);
        BlockingQueue<DnsPacket> dnsResponsesQueue = new ArrayBlockingQueue<>(1000);

        // Ip4Header
        byte version = (byte) 4;
        byte IHL = (byte) 5;
        int headerLength = 1;
        short typeOfService = 1;
        int totalLength = 1;
        int identificationAndFlagsAndFragmentOffset = 1;
        short TTL = 1;
        int protocolNum = 1;
        TransportProtocol protocol = TransportProtocol.UDP;
        int headerChecksum = 1;

        String sourceAddressString = "121.122.123.124";
        InetAddress sourceAddress = buildAddress(sourceAddressString);
        String destinationAddressString = "1.2.3.4";
        InetAddress destinationAddress = buildAddress(destinationAddressString);
        int optionsAndPadding = 1;
        IP4Header ip4Header = new IP4Header(version, IHL, headerLength, typeOfService, totalLength,
                identificationAndFlagsAndFragmentOffset, TTL, protocolNum, protocol, headerChecksum,
                sourceAddress, destinationAddress, optionsAndPadding);

        // UdpHeader
        int sourcePort = 1;
        int destinationPort = 2;
        UdpHeader udpHeader = new UdpHeader(sourcePort, destinationPort);

        // DnsHeader
        int identification = 1;
        int flags = 1;
        int nQuestions = 1;
        int nAnswers = 1;
        int nAuthorityResourceRecords = 0;
        int nAdditionalRRs = 0;
        DnsHeader dnsHeader = new DnsHeader(identification, flags, nQuestions, nAnswers,
                nAuthorityResourceRecords, nAdditionalRRs);

        DnsPacket dnsPacket = new DnsPacket(ip4Header, udpHeader, dnsHeader);
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
        assertEquals((byte) typeOfService, buffer.get());
        assertEquals(40, buffer.getShort());
        assertEquals(81920, buffer.getInt()); // Id and flags and fragment offset
        assertEquals((byte) TTL, buffer.get());
        assertEquals((byte) TransportProtocol.UDP.getNumber(), buffer.get());
        buffer.getShort(); // We don't care about checksum
        assertAddress(sourceAddress, buffer);
        assertAddress(destinationAddress, buffer);

        // UdpHeader
        assertEquals(sourcePort, buffer.getShort());
        assertEquals(destinationPort, buffer.getShort());
        assertEquals(20, buffer.getShort());
        buffer.getShort(); // We don't care about checksum

        // DnsHeader
        assertEquals(identification, buffer.getShort());
        assertEquals(flags, buffer.getShort());
        assertEquals(nQuestions, buffer.getShort());
        assertEquals(nAnswers, buffer.getShort());
        assertEquals(nAuthorityResourceRecords, buffer.getShort());
        assertEquals(nAdditionalRRs, buffer.getShort());

        assertEquals(40, buffer.position());
    }

    private void assertAddress(InetAddress address, ByteBuffer buffer) {
        int ipv4 = 4;
        byte[] addressBytes = new byte[ipv4];
        buffer.get(addressBytes, 0, ipv4);
        byte[] sourceAddressResult = address.getAddress();
        for (int i = 0; i < ipv4; i++) {
            assertEquals(sourceAddressResult[i], addressBytes[i]);
        }
    }
}
