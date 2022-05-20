package com.mocyx.basic_client.handler;

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
    protected final static String TAG = "DnsDownWorker";
    public final AtomicInteger ipId;
    private final int headerSize;


    public DnsDownWorker(BlockingQueue<ByteBuffer> networkToDeviceQueue, BlockingQueue<DnsPacket> dnsResponsesQueue) {
        this.dnsResponsesQueue = dnsResponsesQueue;
        this.networkToDeviceQueue = networkToDeviceQueue;
        ipId = new AtomicInteger();
        this.headerSize = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE;
    }


    private void updateUdpHeader(DnsPacket dnsResponse) {
        ByteBuffer backingBuffer = dnsResponse.getBackingBuffer();
        byte[] data = new byte[dnsResponse.getBackingBuffer().remaining()];
        backingBuffer.get(data);

        int dataLen = Optional.ofNullable(data).map(dataAux -> dataAux.length).orElse(0);

        IpUtil.updateIdentificationAndFlagsAndFragmentOffset(dnsResponse, ipId.addAndGet(1));
        // DnsPacket packet = IpUtil.buildUdpPacket(sourceAddress, destinationAddress, ipId.addAndGet(1));

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        byteBuffer.position(this.headerSize);

        if (data != null) {
            if (byteBuffer.remaining() < data.length) { // TODO: maybe this could be dataLen? why ask for length again?
                System.currentTimeMillis();
            }
            byteBuffer.put(data);
        }
        dnsResponse.updateUDPBuffer(byteBuffer, dataLen);
        byteBuffer.position(this.headerSize + dataLen);
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

