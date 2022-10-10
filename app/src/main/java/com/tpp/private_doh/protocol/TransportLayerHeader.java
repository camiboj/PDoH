package com.tpp.private_doh.protocol;

import java.nio.ByteBuffer;

public interface TransportLayerHeader {
    void fillBuffer(ByteBuffer buffer);
    boolean isTCP();
    boolean isUDP();
}
