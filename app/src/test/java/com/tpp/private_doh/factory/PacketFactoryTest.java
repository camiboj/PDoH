package com.tpp.private_doh.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.util.Helper;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.factory.PacketFactory;
import com.tpp.private_doh.protocol.Packet;

import org.junit.Test;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class PacketFactoryTest extends Helper {

    @Test
    public void packetFactoryCreatesDnsPacketOk() throws UnknownHostException {
        DnsPacket dnsPacket = buildDnsPacket();
        dnsPacket.fillBackingBuffer();
        ByteBuffer buffer = dnsPacket.getBackingBuffer();
        buffer.position(0);
        Packet packetResult = PacketFactory.createPacket(buffer);
        assertTrue(packetResult.isDNS());
        assertTrue(packetResult.isUDP());
        assertFalse(packetResult.isTCP());
        DnsPacket dnsPacketResult = (DnsPacket) packetResult;
        assertEquals(dnsPacket.getBackingBuffer(), dnsPacketResult.getBackingBuffer());
    }

    @Test
    public void packetFactoryCreatesUdpPacketOk() throws UnknownHostException {
        Packet udpPacket = buildUdpPacket();
        ByteBuffer buffer = udpPacket.getBackingBuffer();
        buffer.position(0);
        Packet packetResult = PacketFactory.createPacket(buffer);
        assertTrue(packetResult.isUDP());
        assertFalse(packetResult.isDNS());
        assertFalse(packetResult.isTCP());
        assertEquals(udpPacket.getBackingBuffer(), packetResult.getBackingBuffer());
    }

    @Test
    public void packetFactoryCreatesTcpPacketOk() throws UnknownHostException {
        Packet tcpPacket = buildTcpPacket();
        ByteBuffer buffer = tcpPacket.getBackingBuffer();
        buffer.position(0);
        Packet packetResult = PacketFactory.createPacket(buffer);
        assertTrue(packetResult.isTCP());
        assertFalse(packetResult.isDNS());
        assertFalse(packetResult.isUDP());
        assertEquals(tcpPacket.getBackingBuffer(), packetResult.getBackingBuffer());
    }

    @Test
    public void packetFactoryCreatesDnsPacketFromByteBufferOk() throws UnknownHostException {
        DnsPacket dnsPacket = buildDnsPacket();
        dnsPacket.fillBackingBuffer();
        ByteBuffer buffer = dnsPacket.getBackingBuffer();
        buffer.position(0);
        Packet packet = PacketFactory.createDnsPacket(buffer);
        assertTrue(packet.isDNS());
        DnsPacket dnsPacketResult = (DnsPacket) packet;
        assertEquals(dnsPacket.getBackingBuffer(), dnsPacketResult.getBackingBuffer());
    }
}
