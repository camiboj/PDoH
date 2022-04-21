package com.mocyx.basic_client;

import android.util.Log;

import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.util.ByteBufferPool;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.BlockingQueue;

public class VPNRunnable implements Runnable {
    private static final String TAG = VPNRunnable.class.getSimpleName();

    private FileDescriptor vpnFileDescriptor;
    private BlockingQueue<Packet> deviceToNetworkUDPQueue;
    private BlockingQueue<ByteBuffer> networkToDeviceQueue;

    public VPNRunnable(FileDescriptor vpnFileDescriptor,
                       BlockingQueue<Packet> deviceToNetworkUDPQueue,
                       BlockingQueue<ByteBuffer> networkToDeviceQueue) {
        this.vpnFileDescriptor = vpnFileDescriptor;
        this.deviceToNetworkUDPQueue = deviceToNetworkUDPQueue;
        this.networkToDeviceQueue = networkToDeviceQueue;
    }

    @Override
    public void run() {
        Log.i(TAG, "Started");
        FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
        FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();
        Thread t = new Thread(new WriteVpnThread(vpnOutput, networkToDeviceQueue));
        t.start();
        try {
            ByteBuffer bufferToNetwork;
            while (!Thread.interrupted()) {
                bufferToNetwork = ByteBufferPool.acquire();
                int readBytes = vpnInput.read(bufferToNetwork);

                MainActivity.upByte.addAndGet(readBytes);

                if (readBytes > 0) {
                    bufferToNetwork.flip();
                    Packet packet = new Packet(bufferToNetwork);
                    if (packet.isUDP()) {
                        Log.i(TAG, "read udp" + readBytes);
                        if (packet.isDNS()) {
                            Log.i(TAG, "[dns] this is a dns message");
                            // TODO: when the mvp is ready, this won't be needed because the packet must not be offered to deviceToNetworkUDPQueue
                            ByteBuffer copyBackingBuffer = packet.getBackingBuffer().duplicate();

                            DnsToNetworkController.process(copyBackingBuffer);
                        }
                        // TODO: when the mvp is ready the packet must not be offered to deviceToNetworkUDPQueue
                        deviceToNetworkUDPQueue.offer(packet);
                    }
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            Log.w(TAG, e.toString(), e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.closeResources(vpnInput, vpnOutput);
        }
    }
}

