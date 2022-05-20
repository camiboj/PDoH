package com.mocyx.basic_client.handler;

import android.util.Log;

import com.mocyx.basic_client.protocol.IpUtil;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.protocol.PacketFactory;
import com.mocyx.basic_client.util.ByteBufferPool;

import java.io.IOException;
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
    public final AtomicInteger ipId;
    private final BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private final BlockingQueue<UdpTunnel> tunnelQueue;
    private final Selector selector;
    private final int headerSize;
    protected final static String TAG = "UdpDownWorker";

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

        if (data != null) {
            if (byteBuffer.remaining() < data.length) { // TODO: maybe this could be dataLen? why ask for length again?
                System.currentTimeMillis();
            }
            byteBuffer.put(data);
        }

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
                            Log.e(TAG, "error", e);
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

    private ByteBuffer getData(SelectionKey key) throws IOException{
        DatagramChannel inputChannel = (DatagramChannel) key.channel();
        ByteBuffer receiveBuffer = ByteBufferPool.acquire();
        inputChannel.read(receiveBuffer);
        // Packet packet = PacketFactory.createPacket(bufferDuplicated);
        // Log.i(TAG, "Dns response?: " + packet);
        // if (packet.isDNS()) {
        //     Log.i(TAG, "Dns response: " + packet);
        // }
        return receiveBuffer;
    }
}

