package com.tpp.private_doh.handler;

import android.util.Log;

import com.tpp.private_doh.protocol.IpUtil;
import com.tpp.private_doh.protocol.Packet;
import com.tpp.private_doh.util.ByteBufferPool;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class UdpDownWorker implements Runnable {
    protected final static String TAG = UdpDownWorker.class.getSimpleName();
    public final AtomicInteger ipId;
    private final BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private final BlockingQueue<UdpTunnel> tunnelQueue;
    private final Selector selector;
    private final int headerSize;
    ;

    public UdpDownWorker(Selector selector, BlockingQueue<ByteBuffer> networkToDeviceQueue,
                         BlockingQueue<UdpTunnel> tunnelQueue) {
        this.networkToDeviceQueue = networkToDeviceQueue;
        this.tunnelQueue = tunnelQueue;
        this.selector = selector;
        this.ipId = new AtomicInteger();
        this.headerSize = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE;
    }

    private void sendUdpPack(UdpTunnel tunnel, byte[] data) throws IOException {
        int dataLen = Optional.ofNullable(data).map(dataAux -> dataAux.length).orElse(0);
        Packet packet = IpUtil.buildUdpPacket(tunnel.getRemote(), tunnel.getLocal(), ipId.addAndGet(1));

        ByteBuffer byteBuffer = ByteBufferPool.acquire();

        byteBuffer.position(this.headerSize);
        Optional.ofNullable(data).ifPresent(byteBuffer::put);
        packet.updateUDPBuffer(byteBuffer, dataLen);
        byteBuffer.position(this.headerSize + dataLen);

        this.networkToDeviceQueue.offer(byteBuffer);
    }

    @Override
    public void run() {
        try {
            while (true) {
                int readyChannels = selector.select();
                while (true) {
                    UdpTunnel tunnel = tunnelQueue.poll();
                    if (tunnel == null) {
                        break;
                    } else {
                        try {
                            SelectionKey key = tunnel.getChannel().register(selector, SelectionKey.OP_READ, tunnel);
                            key.interestOps(SelectionKey.OP_READ);
                        } catch (IOException e) {
                            Log.d(TAG, "Register failed", e);
                        }
                    }
                }
                if (readyChannels == 0) {
                    selector.selectedKeys().clear();
                    continue;
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (key.isValid() && key.isReadable()) {
                        try {
                            ByteBuffer receiveBuffer = getData(key);
                            receiveBuffer.flip();
                            byte[] data = new byte[receiveBuffer.remaining()];
                            receiveBuffer.get(data);
                            sendUdpPack((UdpTunnel) key.attachment(), data);
                        } catch (IOException e) {
                            Log.e(TAG, "Error in UdpDownWorker", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in UdpDownWorker", e);
        } finally {
            Log.d(TAG, "UdpDownWorker quit");
        }
    }

    private ByteBuffer getData(SelectionKey key) throws IOException {
        DatagramChannel inputChannel = (DatagramChannel) key.channel();
        ByteBuffer receiveBuffer = ByteBufferPool.acquire();
        try {
            inputChannel.read(receiveBuffer);
        } catch (PortUnreachableException e) {
            Log.i(TAG, String.format("Ping has failed for: %s", inputChannel.getRemoteAddress().toString()));
        }
        return receiveBuffer;
    }
}

