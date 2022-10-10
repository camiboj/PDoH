package com.tpp.private_doh.handler;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.protocol.IpUtil;
import com.tpp.private_doh.protocol.Packet;
import com.tpp.private_doh.util.ByteBufferPool;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DnsDownWorker implements Runnable {
    protected final static String TAG = DnsDownWorker.class.getSimpleName();
    public final AtomicInteger ipId;
    private final BlockingQueue<DnsPacket> dnsResponsesQueue;
    private final BlockingQueue<ByteBuffer> networkToDeviceQueue;

    public DnsDownWorker(BlockingQueue<ByteBuffer> networkToDeviceQueue,
                         BlockingQueue<DnsPacket> dnsResponsesQueue) {
        this.networkToDeviceQueue = networkToDeviceQueue;
        this.dnsResponsesQueue = dnsResponsesQueue;
        ipId = new AtomicInteger();
    }

    @Override
    public void run() {
        while (true) {
            try {
                processPacket();
            } catch (InterruptedException e) { // We should stop this manually when creating a button to stop
                e.printStackTrace();
            }
        }
    }

    @VisibleForTesting
    public void processPacket() throws InterruptedException {
        DnsPacket dnsResponse = dnsResponsesQueue.take();
        fillDnsResponse(dnsResponse);
    }

    private void fillDnsResponse(DnsPacket dnsResponse) {
        ByteBuffer backingBuffer = dnsResponse.getBackingBuffer();
        byte[] data = new byte[dnsResponse.getBackingBuffer().remaining()];
        backingBuffer.get(data);
        int dataLen = Optional.of(data).map(dataAux -> dataAux.length).orElse(0);

        IpUtil.updateIdentificationAndFlagsAndFragmentOffset(dnsResponse, ipId.addAndGet(1));

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        dnsResponse.updateUDPBuffer(byteBuffer, dataLen); // Fill udp and ip header
        int headerSize = dnsResponse.getNetworkLayerHeaderSize() + Packet.UDP_HEADER_SIZE;
        byteBuffer.position(headerSize);
        byteBuffer.put(data); // Fill DNS data
        byteBuffer.position(headerSize + dataLen);

        Log.i(TAG, "[dns] About to send dns packet");
        this.networkToDeviceQueue.offer(byteBuffer);
    }
}

