package com.tpp.private_doh.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketUtils {
    public static int read(SocketChannel channel, ByteBuffer byteBuffer) throws IOException {
        return channel.read(byteBuffer);
    }
}
