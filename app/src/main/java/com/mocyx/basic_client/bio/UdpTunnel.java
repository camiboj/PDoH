package com.mocyx.basic_client.bio;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

public class UdpTunnel {
    private final InetSocketAddress local;
    private final InetSocketAddress remote;
    private final DatagramChannel channel;

    public UdpTunnel(InetSocketAddress local, InetSocketAddress remote, DatagramChannel channel) {
        this.local = local;
        this.remote = remote;
        this.channel = channel;
    }

    public InetSocketAddress getRemote() {
        return this.remote;
    }

    public InetSocketAddress getLocal() {
        return this.local;
    }

    public DatagramChannel getChannel() {
        return channel;
    }
}

