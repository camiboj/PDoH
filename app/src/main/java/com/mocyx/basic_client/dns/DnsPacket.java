package com.mocyx.basic_client.dns;

import com.mocyx.basic_client.protocol.IP4Header;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.protocol.UdpHeader;
import com.mocyx.basic_client.util.ByteBufferPool;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DnsPacket extends Packet {
    private DnsHeader header;
    private List<DnsQuestion> questions;
    private List<DnsAnswer> answers = new ArrayList<>();

    public DnsPacket(IP4Header ip4Header, UdpHeader udpHeader, ByteBuffer buffer) {
        super(ip4Header, udpHeader, buffer);
        ByteBuffer bufferDuplicated = buffer.duplicate();
        this.header = new DnsHeader(bufferDuplicated);
        this.questions = new ArrayList<>();
        for (int i = 0; i < header.getNQuestions(); i++) {
            questions.add(new DnsQuestion(bufferDuplicated));
        }
    }

    public DnsPacket() {
        super();
        this.header = new DnsHeader();
        this.questions = new ArrayList<>();
    }

    @Override
    protected void fillHeader(ByteBuffer buffer) {
        super.fillHeader(buffer);
        putOn(buffer);
    }

    public void addAnswer(String name, int type, int ttl, String data) {
        this.header.addAnswer();
        this.answers.add(new DnsAnswer(name, type, ttl, data));
    }

    public void addQuestion(String name, int type) {
        this.header.addQuestion();
        this.questions.add(new DnsQuestion(name, type));
    }

    public List<DnsQuestion> getQuestions() {
        return questions;
    }

    public void putOn(ByteBuffer buff) {
        header.putOn(buff);
        questions.forEach(
                x -> x.putOn(buff)
        );
        answers.forEach(
                x -> x.putOn(buff)
        );
    }

    @Override
    public String toString() {
        String superString = super.getIp4Header().toString() + super.getHeader().toString();
        return superString + "DnsPacket{" + "header=" + header +
                ", questions=" + questions +
                ", answers=" + answers +
                '}';
    }

    public void setResponseTo(DnsPacket other) {
        // THIS IS HORRIBLE I KNOW
        super.setIp4Header(other.getIp4Header().createResponse());
        super.setHeader(((UdpHeader) other.getHeader()).createResponse());

        int headerSize = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE;
        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        byteBuffer.position(headerSize);
        putOn(byteBuffer);
        int dataLen = byteBuffer.position() - headerSize;
        this.updateUDPBuffer(byteBuffer, dataLen);
        byteBuffer.position(headerSize + dataLen);
    }
}

