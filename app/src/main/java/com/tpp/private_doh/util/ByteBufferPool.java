package com.tpp.private_doh.util;

import java.nio.ByteBuffer;

public class ByteBufferPool {
    private static final int BUFFER_SIZE = 1024;

    public static ByteBuffer acquire() {
        return ByteBuffer.allocate(BUFFER_SIZE);
    }

    public static ByteBuffer acquireWithCapacity(int capacity) {
        return ByteBuffer.allocate(capacity);
    }

}

