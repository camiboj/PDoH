package com.mocyx.basic_client.protocol;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TcpPipe {
    public static Integer tunnelIds = 0;
    public final int tunnelId = tunnelIds++;
    public long mySequenceNum = 0;
    public long theirSequenceNum = 0;
    public long myAcknowledgementNum = 0;
    public long theirAcknowledgementNum = 0;
    public String tunnelKey;
    public InetSocketAddress sourceAddress;
    public InetSocketAddress destinationAddress;
    public SocketChannel remote;
    public TCBStatus tcbStatus = TCBStatus.SYN_SENT;
    public boolean upActive = true;
    public boolean downActive = true;
    public int packId = 1;
    public long timestamp = 0L;
    public int synCount = 0;
    public ByteBuffer remoteOutBuffer = ByteBuffer.allocate(8 * 1024);
}