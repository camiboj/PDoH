package com.tpp.private_doh.util;

import android.util.Log;

import com.tpp.private_doh.app.MainActivity;
import com.tpp.private_doh.network.NetworkManager;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static void handleBytes(FileChannel vpnOutput, ByteBuffer bufferFromNetwork) {
        try {
            bufferFromNetwork.flip();

            while (bufferFromNetwork.hasRemaining()) {
                int w = vpnOutput.write(bufferFromNetwork);
                if (w > 0) {
                    MainActivity.downByte.addAndGet(w);
                }
            }

            bufferFromNetwork.clear();
            bufferFromNetwork = null;
            System.gc();
        } catch (Exception e) {
            Log.i(TAG, "WriteVpnThread fail", e);
        }
    }
}
