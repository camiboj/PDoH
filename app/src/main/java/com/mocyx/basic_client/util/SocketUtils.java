package com.mocyx.basic_client.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketUtils {
    public static int read(SocketChannel channel, ByteBuffer byteBuffer) throws IOException {
        return channel.read(byteBuffer);
    }
}
