package com.mocyx.basic_client.handler;

import android.net.VpnService;
import android.util.Log;

import com.mocyx.basic_client.protocol.IP4Header;
import com.mocyx.basic_client.protocol.IpUtil;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.protocol.TCBStatus;
import com.mocyx.basic_client.protocol.TcpHeader;
import com.mocyx.basic_client.protocol.TcpPipe;
import com.mocyx.basic_client.util.ObjAttrUtil;
import com.mocyx.basic_client.util.SocketUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class TcpPacketHandler implements Runnable {
    private static int HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.TCP_HEADER_SIZE;
    private final String TAG = this.getClass().getSimpleName();

    private final BlockingQueue<Packet> queue;
    private final BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private final VpnService vpnService;
    private final ObjAttrUtil objAttrUtil;

    private long tick;
    private Selector selector;
    private Map<String, TcpPipe> pipes;

    public TcpPacketHandler(BlockingQueue<Packet> queue,
                            BlockingQueue<ByteBuffer> networkToDeviceQueue,
                            VpnService vpnService) {
        this.queue = queue;
        this.vpnService = vpnService;
        this.networkToDeviceQueue = networkToDeviceQueue;
        this.objAttrUtil = new ObjAttrUtil();
        this.pipes = new HashMap<>();
        this.tick = 0;
    }

    private TcpPipe initPipe(Packet packet) throws Exception {
        IP4Header ip4Header = packet.getIp4Header();
        TcpHeader tcpHeader = packet.getTcpHeader();

        InetSocketAddress sourceAddress = new InetSocketAddress(ip4Header.getSourceAddress(), tcpHeader.getSourcePort());
        InetSocketAddress destinationAddress = new InetSocketAddress(ip4Header.getDestinationAddress(), tcpHeader.getDestinationPort());

        TcpPipe pipe = new TcpPipe(sourceAddress, destinationAddress, SocketChannel.open());
        SocketChannel remote = pipe.getRemote();

        objAttrUtil.setAttr(remote, "type", "remote");
        objAttrUtil.setAttr(remote, "pipe", pipe);
        remote.configureBlocking(false);
        SelectionKey key = remote.register(selector, SelectionKey.OP_CONNECT);
        objAttrUtil.setAttr(remote, "key", key);
        vpnService.protect(remote.socket());

        boolean b1 = remote.connect(pipe.getDestinationAddress());
        pipe.setTimestamp(System.currentTimeMillis());
        Log.i(TAG, String.format("initPipe %s %s", pipe.getDestinationAddress(), b1));

        return pipe;
    }

    private void sendTcpPack(TcpPipe pipe, byte flag, byte[] data) {
        int dataLen = 0;
        if (data != null) {
            dataLen = data.length;
        }
        Packet packet = IpUtil.buildTcpPacket(pipe.getDestinationAddress(),
                pipe.getSourceAddress(), flag,
                pipe.getMyAcknowledgmentNum(), pipe.getMySequenceNum(),
                pipe.getPackId());
        pipe.addPackId(1);
        ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER_SIZE + dataLen);
        byteBuffer.position(HEADER_SIZE);
        if (data != null) {
            if (byteBuffer.remaining() < data.length) {
                System.currentTimeMillis();
            }
            byteBuffer.put(data);
        }

        packet.updateTCPBuffer(byteBuffer, flag, pipe.getMySequenceNum(),
                pipe.getMyAcknowledgmentNum(), dataLen);
        byteBuffer.position(HEADER_SIZE + dataLen);
        networkToDeviceQueue.offer(byteBuffer);

        if ((flag & (byte) TcpHeader.SYN) != 0) {
            pipe.addMySequenceNum(1);
        }
        if ((flag & (byte) TcpHeader.FIN) != 0) {
            pipe.addMySequenceNum(1);
        }
        if ((flag & (byte) TcpHeader.ACK) != 0) {
            pipe.addMySequenceNum(dataLen);
        }
    }

    private void handleSyn(Packet packet, TcpPipe pipe) {
        if (pipe.getTcbStatus() == TCBStatus.SYN_SENT) {
            pipe.setTcbStatus(TCBStatus.SYN_RECEIVED);
            Log.i(TAG, String.format("handleSyn %s %s", pipe.getDestinationAddress(),
                    pipe.getTcbStatus()));
        }
        Log.i(TAG, String.format("handleSyn  %d %d", pipe.getTunnelId(),
                packet.getPackId()));
        TcpHeader tcpHeader = packet.getTcpHeader();
        if (pipe.getSynCount() == 0) {
            pipe.setMySequenceNum(1);
            pipe.setTheirSequenceNum(tcpHeader.getSequenceNumber());
            pipe.setMyAcknowledgmentNum(tcpHeader.getSequenceNumber() + 1);
            pipe.setTheirAcknowledgmentNum(tcpHeader.getAcknowledgementNumber());
            sendTcpPack(pipe, (byte) (TcpHeader.SYN | TcpHeader.ACK), null);
        } else {
            pipe.setMyAcknowledgmentNum(tcpHeader.getSequenceNumber() + 1);
        }
        pipe.addSynCount(1);
    }

    private void handleRst(TcpPipe pipe) {
        Log.i(TAG, String.format("handleRst %d", pipe.getTunnelId()));
        pipe.setUpActive(false);
        pipe.setDownActive(false);
        cleanPipe(pipe);
        pipe.setTcbStatus(TCBStatus.CLOSE_WAIT);
    }

    private void handleAck(Packet packet, TcpPipe pipe) throws Exception {
        if (pipe.getTcbStatus() == TCBStatus.SYN_RECEIVED) {
            pipe.setTcbStatus(TCBStatus.ESTABLISHED);
            Log.i(TAG, String.format("handleAck %s %s", pipe.getDestinationAddress(),
                    pipe.getTcbStatus()));
        }

        TcpHeader tcpHeader = packet.getTcpHeader();
        int payloadSize = packet.getBackingBuffer().remaining();

        if (payloadSize == 0) {
            return;
        }

        long newAck = tcpHeader.getSequenceNumber() + payloadSize;
        if (newAck <= pipe.getMyAcknowledgmentNum()) {
            return;
        }

        pipe.setMyAcknowledgmentNum(tcpHeader.getSequenceNumber());
        pipe.setTheirAcknowledgmentNum(tcpHeader.getAcknowledgementNumber());

        pipe.addMyAcknowledgmentNum(payloadSize);
        ByteBuffer buffer = pipe.getRemoteOutBufer();
        buffer.put(packet.getBackingBuffer());
        buffer.flip();
        tryFlushWrite(pipe, pipe.getRemote());
        sendTcpPack(pipe, (byte) TcpHeader.ACK, null);
        System.currentTimeMillis();
    }

    private SelectionKey getKey(SocketChannel channel) {
        return (SelectionKey) objAttrUtil.getAttr(channel, "key");
    }

    private boolean tryFlushWrite(TcpPipe pipe, SocketChannel channel) throws Exception {
        ByteBuffer buffer = pipe.getRemoteOutBufer();
        if (pipe.getRemote().socket().isOutputShutdown() && buffer.remaining() != 0) {
            sendTcpPack(pipe, (byte) (TcpHeader.FIN | TcpHeader.ACK), null);
            buffer.compact();
            return false;
        }
        if (!channel.isConnected()) {
            Log.i(TAG, "not yet connected");
            SelectionKey key = (SelectionKey) objAttrUtil.getAttr(channel, "key");
            int ops = key.interestOps() | SelectionKey.OP_WRITE;
            key.interestOps(ops);
            System.currentTimeMillis();
            buffer.compact();
            return false;
        }
        while (buffer.hasRemaining()) {
            int n = 0;
            n = channel.write(buffer);
            if (n > 4000) {
                System.currentTimeMillis();
            }
            Log.i(TAG, String.format("tryFlushWrite write %s", n));
            if (n <= 0) {
                Log.i(TAG, "write fail");
                SelectionKey key = (SelectionKey) objAttrUtil.getAttr(channel, "key");
                int ops = key.interestOps() | SelectionKey.OP_WRITE;
                key.interestOps(ops);
                System.currentTimeMillis();
                buffer.compact();
                return false;
            }
        }
        buffer.clear();
        if (!pipe.getUpActive()) {
            pipe.getRemote().shutdownOutput();
        }
        return true;
    }

    private void closeUpStream(TcpPipe pipe) {
        Log.i(TAG, String.format("closeUpStream %d", pipe.getTunnelId()));
        try {
            SocketChannel remote = pipe.getRemote();
            if (remote != null && remote.isOpen()) {
                if (remote.isConnected()) {
                    remote.shutdownOutput();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, String.format("closeUpStream %d", pipe.getTunnelId()));
        pipe.setUpActive(false);

        if (isClosedTunnel(pipe)) {
            cleanPipe(pipe);
        }
    }

    private void handleFin(Packet packet, TcpPipe pipe) {
        Log.i(TAG, String.format("handleFin %d", pipe.getTunnelId()));
        pipe.setMyAcknowledgmentNum(packet.getTcpHeader().getSequenceNumber() + 1);
        pipe.setTheirAcknowledgmentNum(packet.getTcpHeader().getAcknowledgementNumber());
        sendTcpPack(pipe, (byte) (TcpHeader.ACK), null);
        closeUpStream(pipe);
        pipe.setTcbStatus(TCBStatus.CLOSE_WAIT);

        Log.i(TAG, String.format("handleFin %s %s", pipe.getDestinationAddress(),
                pipe.getTcbStatus()));
    }

    private void handlePacket(TcpPipe pipe, Packet packet) throws Exception {
        boolean end = false;
        TcpHeader tcpHeader = packet.getTcpHeader();
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
            TcpHeader tcpHeader = currentPacket.getTcpHeader();
            int destinationPort = tcpHeader.getDestinationPort();
            int sourcePort = tcpHeader.getSourcePort();
            String ipAndPort = destinationAddress.getHostAddress() + ":" +
                    destinationPort + ":" + sourcePort;

            if (!pipes.containsKey(ipAndPort)) {
                TcpPipe tcpTunnel = initPipe(currentPacket);
                tcpTunnel.setTunnelKey(ipAndPort);
                pipes.put(ipAndPort, tcpTunnel);
            }
            TcpPipe pipe = pipes.get(ipAndPort);
            handlePacket(pipe, currentPacket);
            System.currentTimeMillis();
        }
    }

    private void doAccept() {
        throw new RuntimeException("");
    }

    private void doRead(SocketChannel channel) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);
        String quitType = "";

        TcpPipe pipe = (TcpPipe) objAttrUtil.getAttr(channel, "pipe");

        while (true) {
            buffer.clear();
            int n = SocketUtils.read(channel, buffer);
            Log.i(TAG, String.format("read %s", n));
            if (n == -1) {
                quitType = "fin";
                break;
            } else if (n == 0) {
                break;
            } else {
                if (pipe.getTcbStatus() != TCBStatus.CLOSE_WAIT) {
                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    sendTcpPack(pipe, (byte) (TcpHeader.ACK), data);
                }
            }
        }
        if (quitType.equals("fin")) {
            closeDownStream(pipe);
        }
    }

    private void cleanPipe(TcpPipe pipe) {
        try {
            SocketChannel remote = pipe.getRemote();
            if (remote != null && remote.isOpen()) {
                remote.close();
            }
            pipes.remove(pipe.getTunnelKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeRst(TcpPipe pipe) {
        Log.i(TAG, String.format("closeRst %d", pipe.getTunnelId()));
        cleanPipe(pipe);
        sendTcpPack(pipe, (byte) TcpHeader.RST, null);
        pipe.setUpActive(false);
        pipe.setDownActive(false);
    }

    private void closeDownStream(TcpPipe pipe) throws Exception {
        Log.i(TAG, String.format("closeDownStream %d", pipe.getTunnelId()));
        SocketChannel remote = pipe.getRemote();
        if (remote != null && remote.isConnected()) {
            remote.shutdownInput();
            int ops = getKey(remote).interestOps() & (~SelectionKey.OP_READ);
            getKey(remote).interestOps(ops);
        }

        sendTcpPack(pipe, (byte) (TcpHeader.FIN | TcpHeader.ACK), null);
        pipe.setDownActive(false);
        if (isClosedTunnel(pipe)) {
            cleanPipe(pipe);
        }
    }

    public boolean isClosedTunnel(TcpPipe tunnel) {
        return !tunnel.getUpActive() && !tunnel.getDownActive();
    }

    private void doConnect(SocketChannel socketChannel) throws Exception {
        Log.i(TAG, String.format("tick %s", tick));
        String type = (String) objAttrUtil.getAttr(socketChannel, "type");
        TcpPipe pipe = (TcpPipe) objAttrUtil.getAttr(socketChannel, "pipe");
        SelectionKey key = (SelectionKey) objAttrUtil.getAttr(socketChannel, "key");
        if (type.equals("remote")) {
            boolean b1 = socketChannel.finishConnect();
            Log.i(TAG, String.format("connect %s %s %s", pipe.getDestinationAddress(),
                    b1, System.currentTimeMillis() - pipe.getTimestamp()));
            pipe.setTimestamp(System.currentTimeMillis());
            pipe.getRemoteOutBufer().flip();
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }

    }

    private void doWrite(SocketChannel socketChannel) throws Exception {
        Log.i(TAG, String.format("tick %s", tick));
        TcpPipe pipe = (TcpPipe) objAttrUtil.getAttr(socketChannel, "pipe");
        boolean flushed = tryFlushWrite(pipe, socketChannel);
        if (flushed) {
            SelectionKey key1 = (SelectionKey) objAttrUtil.getAttr(socketChannel, "key");
            key1.interestOps(SelectionKey.OP_READ);
        }
    }

    private void handleSockets() throws Exception {
        while (selector.selectNow() > 0) {
            selector.selectedKeys().forEach(this::handleKey);
        }
    }

    private void handleKey(SelectionKey key) {
        TcpPipe pipe = (TcpPipe) objAttrUtil.getAttr(key.channel(), "pipe");
        if (key.isValid()) {
            try {
                if (key.isAcceptable()) {
                    doAccept();
                } else if (key.isReadable()) {
                    doRead((SocketChannel) key.channel());
                } else if (key.isConnectable()) {
                    doConnect((SocketChannel) key.channel());
                    System.currentTimeMillis();
                } else if (key.isWritable()) {
                    doWrite((SocketChannel) key.channel());
                    System.currentTimeMillis();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                if (pipe != null) {
                    closeRst(pipe);
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
            while (true) {
                handleReadFromVpn();
                handleSockets();
                tick += 1;
                Thread.sleep(1);
            }
        } catch (Exception e) {
            Log.e(e.getMessage(), "", e);
        }
    }
}


