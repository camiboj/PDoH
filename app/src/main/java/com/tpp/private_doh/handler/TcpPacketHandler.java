package com.tpp.private_doh.handler;

import android.net.VpnService;
import android.util.Log;

import com.tpp.private_doh.PDoHVpnService;
import com.tpp.private_doh.app.MainActivity;
import com.tpp.private_doh.config.Config;
import com.tpp.private_doh.protocol.IP4Header;
import com.tpp.private_doh.protocol.IpUtil;
import com.tpp.private_doh.protocol.Packet;
import com.tpp.private_doh.protocol.TCBStatus;
import com.tpp.private_doh.protocol.TcpHeader;
import com.tpp.private_doh.util.ByteBufferPool;
import com.tpp.private_doh.util.NetworkUtils;
import com.tpp.private_doh.util.ObjAttrUtil;
import com.tpp.private_doh.util.SocketUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class TcpPacketHandler implements Runnable {
    private static final String TAG = TcpPacketHandler.class.getSimpleName();

    private static int HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.TCP_HEADER_SIZE;
    private final BlockingQueue<Packet> queue;
    private final BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private final VpnService vpnService;
    private final ObjAttrUtil objAttrUtil;
    private final FileChannel vpnOutput;
    private Selector selector;
    private Map<String, TcpPipe> pipes;
    private boolean firstIteration;
    private ByteBuffer readBuffer;
    private ByteBuffer allocatedBuffer;

    public TcpPacketHandler(BlockingQueue<Packet> queue,
                            BlockingQueue<ByteBuffer> networkToDeviceQueue,
                            VpnService vpnService, FileChannel vpnOutput) {
        this.queue = queue;
        this.vpnService = vpnService;
        this.networkToDeviceQueue = networkToDeviceQueue;
        this.objAttrUtil = new ObjAttrUtil();
        this.pipes = new HashMap<>();
        this.firstIteration = true;
        this.readBuffer = ByteBufferPool.acquireWithCapacity(Config.READ_BUFFER_SIZE);
        this.allocatedBuffer = ByteBufferPool.acquireWithCapacity(Config.READ_BUFFER_SIZE*4); //TODO: change this config, it's only for testing
        this.vpnOutput = vpnOutput;
    }

    private TcpPipe initPipe(Packet packet) throws Exception {
        TcpPipe pipe = new TcpPipe();
        IP4Header ip4Header = packet.getIp4Header();
        TcpHeader tcpHeader = (TcpHeader) packet.getHeader();
        pipe.sourceAddress = new InetSocketAddress(ip4Header.getSourceAddress(), tcpHeader.getSourcePort());
        pipe.destinationAddress = new InetSocketAddress(ip4Header.getDestinationAddress(), tcpHeader.getDestinationPort());
        pipe.remote = SocketChannel.open();
        objAttrUtil.setAttr(pipe.remote, "type", "remote");
        objAttrUtil.setAttr(pipe.remote, "pipe", pipe);
        pipe.remote.configureBlocking(false);
        SelectionKey key = pipe.remote.register(selector, SelectionKey.OP_CONNECT);
        objAttrUtil.setAttr(pipe.remote, "key", key);
        vpnService.protect(pipe.remote.socket());
        boolean b1 = pipe.remote.connect(pipe.destinationAddress);
        pipe.timestamp = System.currentTimeMillis();
        return pipe;
    }

    private void sendTcpPack(TcpPipe pipe, byte flag, byte[] data) {
        int dataLen = 0;
        if (data != null) {
            dataLen = data.length;
        }
        Packet packet = IpUtil.buildTcpPacket(pipe.destinationAddress, pipe.sourceAddress, flag,
                pipe.myAcknowledgementNum, pipe.mySequenceNum, pipe.packId);
        pipe.packId += 1;

        //ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER_SIZE + dataLen);
        allocatedBuffer.clear();

        allocatedBuffer.position(HEADER_SIZE);
        if (data != null) {
            allocatedBuffer.put(data);
        }

        data = null;
        System.gc();

        packet.updateTCPBuffer(allocatedBuffer, flag, pipe.mySequenceNum, pipe.myAcknowledgementNum, dataLen);
        allocatedBuffer.position(HEADER_SIZE + dataLen);

        //Log.i(TAG, String.format("Bytebuffer is size: %d", byteBuffer.limit()));
        //networkToDeviceQueue.offer(allocatedBuffer);

        NetworkUtils.handleBytes(vpnOutput, allocatedBuffer);

        if ((flag & (byte) TcpHeader.SYN) != 0) {
            pipe.mySequenceNum += 1;
        }
        if ((flag & (byte) TcpHeader.FIN) != 0) {
            pipe.mySequenceNum += 1;
        }
        if ((flag & (byte) TcpHeader.ACK) != 0) {
            pipe.mySequenceNum += dataLen;
        }
    }

    private void handleSyn(Packet packet, TcpPipe pipe) {
        if (pipe.tcbStatus == TCBStatus.SYN_SENT) {
            pipe.tcbStatus = TCBStatus.SYN_RECEIVED;
        }
        TcpHeader tcpHeader = (TcpHeader) packet.getHeader();
        if (pipe.synCount == 0) {
            pipe.mySequenceNum = 1;
            pipe.theirSequenceNum = tcpHeader.getSequenceNumber();
            pipe.myAcknowledgementNum = tcpHeader.getSequenceNumber() + 1;
            pipe.theirAcknowledgementNum = tcpHeader.getAcknowledgementNumber();
            sendTcpPack(pipe, (byte) (TcpHeader.SYN | TcpHeader.ACK), null);
        } else {
            pipe.myAcknowledgementNum = tcpHeader.getSequenceNumber() + 1;
        }
        pipe.synCount += 1;
    }

    private void handleRst(TcpPipe pipe) {
        pipe.upActive = false;
        pipe.downActive = false;
        cleanPipe(pipe);
        pipe.tcbStatus = TCBStatus.CLOSE_WAIT;
    }

    private void handleAck(Packet packet, TcpPipe pipe) throws Exception {
        if (pipe.tcbStatus == TCBStatus.SYN_RECEIVED) {
            pipe.tcbStatus = TCBStatus.ESTABLISHED;
        }

        TcpHeader tcpHeader = (TcpHeader) packet.getHeader();
        int payloadSize = packet.getBackingBuffer().remaining();

        if (payloadSize == 0) {
            return;
        }

        long newAck = tcpHeader.getSequenceNumber() + payloadSize;
        if (newAck <= pipe.myAcknowledgementNum) {
            return;
        }

        pipe.myAcknowledgementNum = tcpHeader.getSequenceNumber();
        pipe.theirAcknowledgementNum = tcpHeader.getAcknowledgementNumber();

        pipe.myAcknowledgementNum += payloadSize;
        fillRemoteOutBuffer(pipe, packet.getBackingBuffer());
        pipe.remoteOutBuffer.flip();
        tryFlushWrite(pipe, pipe.remote);
        sendTcpPack(pipe, (byte) TcpHeader.ACK, null);
    }

    private void fillRemoteOutBuffer(TcpPipe pipe, ByteBuffer backingBuffer) {
        boolean filled = false;
        while (!filled) {
            try {
                pipe.remoteOutBuffer.put(backingBuffer);
                filled = true;
            } catch (Exception e) {
                Log.e(TAG, "Creating another buffer");
                int limit = pipe.remoteOutBuffer.limit();
                limit *= 2;
                ByteBuffer auxiliaryBuffer = ByteBufferPool.acquireWithCapacity(limit);
                auxiliaryBuffer.put(pipe.remoteOutBuffer);
                pipe.remoteOutBuffer = null;
                pipe.remoteOutBuffer = auxiliaryBuffer;
            }
        }
    }

    private SelectionKey getKey(SocketChannel channel) {
        return (SelectionKey) objAttrUtil.getAttr(channel, "key");
    }

    private boolean tryFlushWrite(TcpPipe pipe, SocketChannel channel) throws Exception {
        ByteBuffer buffer = pipe.remoteOutBuffer;
        if (pipe.remote.socket().isOutputShutdown() && buffer.remaining() != 0) {
            sendTcpPack(pipe, (byte) (TcpHeader.FIN | TcpHeader.ACK), null);
            buffer.compact();
            return false;
        }
        if (!channel.isConnected()) {
            SelectionKey key = (SelectionKey) objAttrUtil.getAttr(channel, "key");
            int ops = key.interestOps() | SelectionKey.OP_WRITE;
            key.interestOps(ops);
            buffer.compact();
            return false;
        }
        while (buffer.hasRemaining()) {
            int n = channel.write(buffer);
            if (n < 0) {
                SelectionKey key = (SelectionKey) objAttrUtil.getAttr(channel, "key");
                int ops = key.interestOps() | SelectionKey.OP_WRITE;
                key.interestOps(ops);
                buffer.compact();
                return false;
            }
        }
        buffer.clear();
        if (!pipe.upActive) {
            pipe.remote.shutdownOutput();
        }
        return true;
    }

    private void closeUpStream(TcpPipe pipe) {
        try {
            if (pipe.remote != null && pipe.remote.isOpen()) {
                if (pipe.remote.isConnected()) {
                    pipe.remote.shutdownOutput();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pipe.upActive = false;

        if (isClosedTunnel(pipe)) {
            cleanPipe(pipe);
        }
    }

    private void handleFin(Packet packet, TcpPipe pipe) {
        TcpHeader tcpHeader = (TcpHeader) packet.getHeader();
        pipe.myAcknowledgementNum = tcpHeader.getSequenceNumber() + 1;
        pipe.theirAcknowledgementNum = tcpHeader.getAcknowledgementNumber();
        sendTcpPack(pipe, (byte) (TcpHeader.ACK), null);
        closeUpStream(pipe);
        pipe.tcbStatus = TCBStatus.CLOSE_WAIT;
    }

    private void handlePacket(TcpPipe pipe, Packet packet) throws Exception {
        boolean end = false;
        TcpHeader tcpHeader = (TcpHeader) packet.getHeader();
        if (tcpHeader.isSYN()) {
            handleSyn(packet, pipe);
            end = true;
        }
        if (!end && tcpHeader.isRST()) {
            handleRst(pipe);
            return;
        }
        if (!end && tcpHeader.isFIN()) {
            handleFin(packet, pipe);
            end = true;
        }
        if (!end && tcpHeader.isACK()) {
            handleAck(packet, pipe);
        }

    }

    private void handleReadFromVpn() throws Exception {
        while (true) {
            Packet currentPacket = queue.poll();
            if (currentPacket == null) {
                return;
            }
            InetAddress destinationAddress = currentPacket.getIp4Header().getDestinationAddress();
            TcpHeader tcpHeader = (TcpHeader) currentPacket.getHeader();
            int destinationPort = tcpHeader.getDestinationPort();
            int sourcePort = tcpHeader.getSourcePort();
            String ipAndPort = destinationAddress.getHostAddress() + ":" +
                    destinationPort + ":" + sourcePort;

            if (!pipes.containsKey(ipAndPort)) {
                TcpPipe tcpTunnel = initPipe(currentPacket);
                tcpTunnel.tunnelKey = ipAndPort;
                pipes.put(ipAndPort, tcpTunnel);
            }
            TcpPipe pipe = pipes.get(ipAndPort);
            handlePacket(pipe, currentPacket);
        }
    }

    private void doAccept(ServerSocketChannel serverChannel) throws Exception {
        throw new RuntimeException("");
    }

    private void doRead(SocketChannel channel) throws Exception {
        boolean shouldQuit = false;

        TcpPipe pipe = (TcpPipe) objAttrUtil.getAttr(channel, "pipe");

        while (true) {
            readBuffer.clear();
            int n = SocketUtils.read(channel, readBuffer);
            if (n == -1) {
                shouldQuit = true;
                break;
            } else if (n == 0) {
                break;
            } else {
                if (pipe.tcbStatus != TCBStatus.CLOSE_WAIT) {
                    readBuffer.flip();
                    byte[] data = new byte[readBuffer.remaining()];
                    readBuffer.get(data);
                    sendTcpPack(pipe, (byte) (TcpHeader.ACK), data);
                }
            }
        }
        if (shouldQuit) {
            closeDownStream(pipe);
        }
    }

    private void cleanPipe(TcpPipe pipe) {
        try {
            if (pipe.remote != null && pipe.remote.isOpen()) {
                pipe.remote.close();
            }
            pipes.remove(pipe.tunnelKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeRst(TcpPipe pipe) {
        cleanPipe(pipe);
        sendTcpPack(pipe, (byte) TcpHeader.RST, null);
        pipe.upActive = false;
        pipe.downActive = false;
    }

    private void closeDownStream(TcpPipe pipe) throws Exception {
        if (pipe.remote != null && pipe.remote.isConnected()) {
            pipe.remote.shutdownInput();
            int ops = getKey(pipe.remote).interestOps() & (~SelectionKey.OP_READ);
            getKey(pipe.remote).interestOps(ops);
        }

        sendTcpPack(pipe, (byte) (TcpHeader.FIN | TcpHeader.ACK), null);
        pipe.downActive = false;
        if (isClosedTunnel(pipe)) {
            cleanPipe(pipe);
        }
    }

    public boolean isClosedTunnel(TcpPipe tunnel) {
        return !tunnel.upActive && !tunnel.downActive;
    }

    private void doConnect(SocketChannel socketChannel) throws Exception {
        String type = (String) objAttrUtil.getAttr(socketChannel, "type");
        TcpPipe pipe = (TcpPipe) objAttrUtil.getAttr(socketChannel, "pipe");
        SelectionKey key = (SelectionKey) objAttrUtil.getAttr(socketChannel, "key");
        if (type.equals("remote")) {
            boolean b1 = socketChannel.finishConnect();
            pipe.timestamp = System.currentTimeMillis();
            pipe.remoteOutBuffer.flip();
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }

    }

    private void doWrite(SocketChannel socketChannel) throws Exception {
        TcpPipe pipe = (TcpPipe) objAttrUtil.getAttr(socketChannel, "pipe");
        boolean flushed = tryFlushWrite(pipe, socketChannel);
        if (flushed) {
            SelectionKey key1 = (SelectionKey) objAttrUtil.getAttr(socketChannel, "key");
            key1.interestOps(SelectionKey.OP_READ);
        }
    }


    private void handleSockets() throws Exception {
        while (selector.selectNow() > 0) {
            for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                SelectionKey key = it.next();
                it.remove();
                TcpPipe pipe = (TcpPipe) objAttrUtil.getAttr(key.channel(), "pipe");
                if (key.isValid()) {
                    try {
                        if (key.isAcceptable()) {
                            doAccept((ServerSocketChannel) key.channel());
                        } else if (key.isReadable()) {
                            doRead((SocketChannel) key.channel());
                        } else if (key.isConnectable()) {
                            doConnect((SocketChannel) key.channel());
                        } else if (key.isWritable()) {
                            doWrite((SocketChannel) key.channel());
                        }
                    } catch (Exception e) {
                        if (pipe != null) {
                            closeRst(pipe);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
            while (true) {
                try {
                    handleReadFromVpn();
                } catch (IOException e) {
                    Log.e(TAG, "There was an error reading from VPN", e);
                }
                try {
                    handleSockets();
                } catch (IOException e) {
                    Log.e(TAG, "There was an error in socketHandling", e);
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Log.i(TAG, "The execution was interrupted");
        } catch (Exception e) {
            Log.e(e.getMessage(), "", e);
        }
    }

    private static class TcpPipe {
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
        int synCount = 0;
        private ByteBuffer remoteOutBuffer = ByteBufferPool.acquireWithCapacity(Config.TCP_BUFFER_BYTES);
    }
}

