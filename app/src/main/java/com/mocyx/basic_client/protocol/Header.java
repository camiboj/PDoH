package com.mocyx.basic_client.protocol;

import java.nio.ByteBuffer;

public interface Header {
    void fillBuffer(ByteBuffer buffer);
    boolean isTCP();
    boolean isUDP();
}
