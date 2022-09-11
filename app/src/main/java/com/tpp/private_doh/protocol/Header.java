package com.tpp.private_doh.protocol;

import java.nio.ByteBuffer;

public interface Header {
    void fillBuffer(ByteBuffer buffer);
    boolean isTCP();
    boolean isUDP();
}
