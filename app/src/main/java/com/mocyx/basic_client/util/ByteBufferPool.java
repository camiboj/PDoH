package com.mocyx.basic_client.util;

import java.nio.ByteBuffer;

public class ByteBufferPool {
    private static final int BUFFER_SIZE = 16384;

    public static ByteBuffer acquire() {
        return ByteBuffer.allocate(BUFFER_SIZE);
    }
}

