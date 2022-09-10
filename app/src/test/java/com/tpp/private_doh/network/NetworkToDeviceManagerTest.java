package com.tpp.private_doh.network;

import static org.junit.Assert.assertEquals;

import com.tpp.private_doh.util.ByteBufferPool;
import com.tpp.private_doh.util.ResourceUtils;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class NetworkToDeviceManagerTest {
    @Test
    public void networkToDeviceManagerHandleBytesOk() throws IOException {
        String testPath = "testPath.txt";
        BlockingQueue<ByteBuffer> networkToDeviceQueue = new ArrayBlockingQueue<>(100);
        FileChannel vpnOutput = new FileOutputStream(testPath).getChannel();
        FileChannel vpnInput = new FileInputStream(testPath).getChannel();

        String text = "This is a text";
        ByteBuffer buffer = ByteBufferPool.acquire();
        buffer.put(text.getBytes(StandardCharsets.UTF_8), 0, text.length());
        networkToDeviceQueue.offer(buffer);
        NetworkToDeviceManager networkToDeviceManager = new NetworkToDeviceManager(vpnOutput,
                networkToDeviceQueue);

        networkToDeviceManager.handleBytes();

        ByteBuffer bufferRead = ByteBuffer.allocate(text.length());
        int nBytesRead = vpnInput.read(bufferRead);
        assertEquals(text.length(), nBytesRead);
        String fileContent = new String(bufferRead.array(), StandardCharsets.UTF_8);
        assertEquals(text, fileContent);

        // Close resources
        ResourceUtils.closeResources(vpnInput, vpnInput);
        File file = new File(testPath);
        file.delete();
    }
}
