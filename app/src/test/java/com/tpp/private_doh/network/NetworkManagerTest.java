package com.tpp.private_doh.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.util.Helper;

import com.tpp.private_doh.controller.PingController;
import com.tpp.private_doh.controller.ProtocolId;
import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.factory.ShardingControllerFactory;
import com.tpp.private_doh.protocol.Packet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

@RunWith(MockitoJUnitRunner.class)
public class NetworkManagerTest extends Helper {

    @Mock
    FileChannel vpnInput;

    @Mock
    FileChannel vpnOutput;

    @Mock
    ExecutorService dnsWorkers;

    @Mock
    PingController pingController;

    private NetworkManager networkManager;
    private BlockingQueue<Packet> deviceToNetworkUDPQueue;
    private BlockingQueue<Packet> deviceToNetworkTCPQueue;

    @Before
    public void setUp() {
        this.deviceToNetworkUDPQueue = new ArrayBlockingQueue<>(1000);
        this.deviceToNetworkTCPQueue = new ArrayBlockingQueue<>(1000);
        BlockingQueue<DnsPacket> dnsResponsesQueue = new ArrayBlockingQueue<>(1000);
        BlockingQueue<ByteBuffer> networkToDeviceQueue = new ArrayBlockingQueue<>(1000);

        ShardingControllerFactory.setProtocolId(ProtocolId.DOH);
        this.networkManager = new NetworkManager(vpnInput, vpnOutput, deviceToNetworkUDPQueue,
                deviceToNetworkTCPQueue, dnsResponsesQueue, networkToDeviceQueue, dnsWorkers, pingController);
    }

    @Test
    public void testNetworkManagerHandlesDnsPacketsOk() throws IOException {
        DnsPacket dnsPacket = buildDnsPacket();
        dnsPacket.fillBackingBuffer();
        ByteBuffer buffer = dnsPacket.getBackingBuffer();
        int nBytes = 74;
        buffer.position(nBytes);
        when(vpnInput.read(buffer)).thenReturn(nBytes);

        networkManager.processPackets(buffer);

        verify(dnsWorkers).submit((Runnable) any());
        assertTrue(deviceToNetworkUDPQueue.isEmpty());
        assertTrue(deviceToNetworkTCPQueue.isEmpty());
    }

    @Test
    public void testNetworkManagerHandlesUdpPacketsOk() throws IOException {
        Packet udpPacket = buildUdpPacket();
        ByteBuffer buffer = udpPacket.getBackingBuffer();
        int nBytes = 28;
        buffer.position(nBytes);
        when(vpnInput.read(buffer)).thenReturn(nBytes);

        networkManager.processPackets(buffer);

        verifyNoInteractions(dnsWorkers);
        assertTrue(deviceToNetworkTCPQueue.isEmpty());
        assertEquals(1, deviceToNetworkUDPQueue.size());
        Packet packetResult = deviceToNetworkUDPQueue.peek();
        assertNotNull(packetResult);
        assertEquals(udpPacket.getBackingBuffer(), packetResult.getBackingBuffer());
    }

    @Test
    public void testNetworkManagerHandlesTcpPacketsOk() throws IOException {
        Packet tcpPacket = buildTcpPacket();
        ByteBuffer buffer = tcpPacket.getBackingBuffer();
        int nBytes = 40;
        buffer.position(nBytes);
        when(vpnInput.read(buffer)).thenReturn(nBytes);

        networkManager.processPackets(buffer);

        verifyNoInteractions(dnsWorkers);
        assertTrue(deviceToNetworkUDPQueue.isEmpty());
        assertEquals(1, deviceToNetworkTCPQueue.size());
        Packet packetResult = deviceToNetworkTCPQueue.peek();
        assertNotNull(packetResult);
        assertEquals(tcpPacket.getBackingBuffer(), packetResult.getBackingBuffer());
    }
}
