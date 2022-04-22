package com.mocyx.basic_client.protocol;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TcpPipe {
    private static Integer tunnelIds;
    private final int tunnelId;
    private long mySequenceNum;
    private long theirSequenceNum;
    private long myAcknowledgementNum;
    private long theirAcknowledgementNum;
    private String tunnelKey;
    private InetSocketAddress sourceAddress;
    private InetSocketAddress destinationAddress;
    private SocketChannel remote;
    private TCBStatus tcbStatus;
    private boolean upActive;
    private boolean downActive;
    private int packId;
    private long timestamp;
    private int synCount;
    private ByteBuffer remoteOutBuffer;

    public TcpPipe(InetSocketAddress sourceAddress,
                   InetSocketAddress destinationAddress, SocketChannel remote) {
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.remote = remote;
        this.tunnelIds = 0;
        this.tunnelId = tunnelIds++;
        this.mySequenceNum = 0;
        this.theirSequenceNum = 0;
        this.myAcknowledgementNum = 0;
        this.theirAcknowledgementNum = 0;
        this.tcbStatus = TCBStatus.SYN_SENT;
        this.upActive = true;
        this.downActive = true;
        this.packId = 1;
        this.timestamp = 0L;
        this.synCount = 0;
        this.remoteOutBuffer = ByteBuffer.allocate(8 * 1024);
    }

    public SocketChannel getRemote() {
        return remote;
    }

    public InetSocketAddress getDestinationAddress() {
        return destinationAddress;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public TCBStatus getTcbStatus() {
        return tcbStatus;
    }

    public void setTcbStatus(TCBStatus tcbStatus) {
        this.tcbStatus = tcbStatus;
    }

    public int getTunnelId() {
        return tunnelId;
    }

    public void setUpActive(boolean upActive) {
        this.upActive = upActive;
    }

    public void setDownActive(boolean downActive) {
        this.downActive = downActive;
    }

    public ByteBuffer getRemoteOutBufer() {
        return remoteOutBuffer;
    }

    public long getMySequenceNum() {
        return mySequenceNum;
    }

    public long getMyAcknowledgmentNum() {
        return myAcknowledgementNum;
    }

    public void addMySequenceNum(int num) {
        this.mySequenceNum += num;
    }

    public int getSynCount() {
        return synCount;
    }

    public void setMySequenceNum(int sequenceNum) {
        this.mySequenceNum = sequenceNum;
    }

    public void setTheirSequenceNum(long sequenceNum) {
        this.theirSequenceNum = sequenceNum;
    }

    public void setMyAcknowledgmentNum(long acknowledgementNum) {
        this.myAcknowledgementNum = acknowledgementNum;
    }

    public void setTheirAcknowledgmentNum(long acknowledgementNum) {
        this.theirAcknowledgementNum = acknowledgementNum;
    }

    public void addSynCount(int i) {
        this.synCount += i;
    }

    public InetSocketAddress getSourceAddress() {
        return sourceAddress;
    }

    public int getPackId() {
        return packId;
    }

    public void addPackId(int i) {
        this.packId += i;
    }

    public void addMyAcknowledgmentNum(int i) {
        this.myAcknowledgementNum += i;
    }

    public boolean getUpActive() {
        return upActive;
    }

    public void setTunnelKey(String tunnelKey) {
        this.tunnelKey = tunnelKey;
    }

    public String getTunnelKey() {
        return tunnelKey;
    }

    public boolean getDownActive() {
        return downActive;
    }

    public long getTimestamp() {
        return timestamp;
    }
}