package com.mocyx.basic_client;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.BlockingQueue;

public class WriteVpnThread implements Runnable {
    private final String TAG = WriteVpnThread.class.getSimpleName();

    FileChannel vpnOutput;
    private BlockingQueue<ByteBuffer> networkToDeviceQueue;

    public WriteVpnThread(FileChannel vpnOutput, BlockingQueue<ByteBuffer> networkToDeviceQueue) {
        this.vpnOutput = vpnOutput;
        this.networkToDeviceQueue = networkToDeviceQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ByteBuffer bufferFromNetwork = networkToDeviceQueue.take();
                bufferFromNetwork.flip();
                while (bufferFromNetwork.hasRemaining()) {
                    int w = vpnOutput.write(bufferFromNetwork);
                    if (w > 0) {
                        MainActivity.downByte.addAndGet(w);
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, "WriteVpnThread fail", e);
            }
        }
    }
}

