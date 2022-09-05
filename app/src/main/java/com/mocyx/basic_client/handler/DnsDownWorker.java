package com.mocyx.basic_client.handler;

import android.util.Log;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.protocol.IpUtil;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.util.ByteBufferPool;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DnsDownWorker implements Runnable {
    protected final static String TAG = DnsDownWorker.class.getSimpleName();
    public final AtomicInteger ipId;
    private final BlockingQueue<DnsPacket> dnsResponsesQueue;
    ;
    private final BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private final int headerSize;


    public DnsDownWorker(BlockingQueue<ByteBuffer> networkToDeviceQueue, BlockingQueue<DnsPacket> dnsResponsesQueue) {
        this.dnsResponsesQueue = dnsResponsesQueue;
        this.networkToDeviceQueue = networkToDeviceQueue;
        ipId = new AtomicInteger();
        this.headerSize = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE;
    }

    @Override
    public void run() {
        while (true) {
            try {
                DnsPacket dnsResponse = dnsResponsesQueue.take();
                fillDnsResponse(dnsResponse);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void fillDnsResponse(DnsPacket dnsResponse) {
        ByteBuffer backingBuffer = dnsResponse.getBackingBuffer();
        byte[] data = new byte[dnsResponse.getBackingBuffer().remaining()];
        backingBuffer.get(data);
        int dataLen = Optional.of(data).map(dataAux -> dataAux.length).orElse(0);

        IpUtil.updateIdentificationAndFlagsAndFragmentOffset(dnsResponse, ipId.addAndGet(1));

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        dnsResponse.updateUDPBuffer(byteBuffer, dataLen); // Fill udp and ip header
        byteBuffer.position(this.headerSize);
        byteBuffer.put(data); // Fill DNS data
        byteBuffer.position(this.headerSize + dataLen);

        Log.i(TAG, "[dns] About to send dns packet");
        this.networkToDeviceQueue.offer(byteBuffer);
    }
}

