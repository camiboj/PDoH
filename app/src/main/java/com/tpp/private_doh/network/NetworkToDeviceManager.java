package com.tpp.private_doh.network;

import android.util.Log;

import com.tpp.private_doh.app.MainActivity;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.BlockingQueue;

public class NetworkToDeviceManager implements Runnable {
    private static final String TAG = NetworkToDeviceManager.class.getSimpleName();
    FileChannel vpnOutput;
    private BlockingQueue<ByteBuffer> networkToDeviceQueue;

    NetworkToDeviceManager(FileChannel vpnOutput, BlockingQueue<ByteBuffer> networkToDeviceQueue) {
        this.vpnOutput = vpnOutput;
        this.networkToDeviceQueue = networkToDeviceQueue;
    }

    @Override
    public void run() {
        while (true) {
            handleBytes();
        }
    }

    public void handleBytes() {
        try {
            ByteBuffer bufferFromNetwork = networkToDeviceQueue.take();
            bufferFromNetwork.flip();

            while (bufferFromNetwork.hasRemaining()) {
                int w = vpnOutput.write(bufferFromNetwork);
                if (w > 0) {
                    MainActivity.downByte.addAndGet(w);
                }
            }
            bufferFromNetwork.clear();
        } catch (Exception e) {
            Log.i(TAG, "WriteVpnThread fail", e);
        }
    }
}