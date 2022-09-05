package com.tpp.private_doh.util;

import java.nio.ByteBuffer;

public class ByteBufferPool {
    private static final int BUFFER_SIZE = 16384;

    public static ByteBuffer acquire() {
        return ByteBuffer.allocate(BUFFER_SIZE);
    }
}

