package com.mocyx.basic_client.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;

public class DnsDownWorker extends UdpDownWorker {
    private final BlockingQueue<ByteBuffer> dnsResponsesQueue;
    protected final static String TAG = "DnsDownWorker";

    public DnsDownWorker(Selector selector, BlockingQueue<ByteBuffer> networkToDeviceQueue,
                         BlockingQueue<UdpTunnel> tunnelQueue, BlockingQueue<ByteBuffer> dnsResponsesQueue) {
        super(selector, networkToDeviceQueue, tunnelQueue);
        this.dnsResponsesQueue = dnsResponsesQueue;
    }

    @Override
    protected ByteBuffer getData(SelectionKey key) throws IOException, InterruptedException {
        return dnsResponsesQueue.take();
    }
}

