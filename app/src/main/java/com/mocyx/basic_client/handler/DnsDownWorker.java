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
    private final BlockingQueue<DnsPacket> dnsResponsesQueue;
    private final BlockingQueue<ByteBuffer> networkToDeviceQueue;
    protected final static String TAG = DnsDownWorker.class.getSimpleName();;
    public final AtomicInteger ipId;
    private final int headerSize;


    public DnsDownWorker(BlockingQueue<ByteBuffer> networkToDeviceQueue, BlockingQueue<DnsPacket> dnsResponsesQueue) {
        this.dnsResponsesQueue = dnsResponsesQueue;
        this.networkToDeviceQueue = networkToDeviceQueue;
        ipId = new AtomicInteger();
        this.headerSize = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE;
    }


    private void updateUdpHeader(DnsPacket dnsResponse) {
        // TODO: response is bad mapped here. We should do something to

        // Get the len of the data
        ByteBuffer backingBuffer = dnsResponse.getBackingBuffer();
        byte[] data = new byte[dnsResponse.getBackingBuffer().remaining()];
        backingBuffer.get(data);
        int dataLen = Optional.ofNullable(data).map(dataAux -> dataAux.length).orElse(0);

        IpUtil.updateIdentificationAndFlagsAndFragmentOffset(dnsResponse, ipId.addAndGet(1));

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        dnsResponse.updateUDPBuffer(byteBuffer, dataLen); // Fill udp and ip header
        byteBuffer.position(this.headerSize);
        byteBuffer.put(data); // Fill DNS data
        byteBuffer.position(this.headerSize + dataLen);

        Log.i(TAG, "[dns] about to send dns packet");
        this.networkToDeviceQueue.offer(byteBuffer);
    }

    @Override
    public void run() {
        while (true) {
            try {
                DnsPacket dnsResponse = dnsResponsesQueue.take();
                updateUdpHeader(dnsResponse);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

