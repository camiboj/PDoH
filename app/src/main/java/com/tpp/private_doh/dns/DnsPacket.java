package com.tpp.private_doh.dns;

import com.tpp.private_doh.protocol.IP4Header;
import com.tpp.private_doh.protocol.Packet;
import com.tpp.private_doh.protocol.UdpHeader;
import com.tpp.private_doh.util.ByteBufferPool;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DnsPacket extends Packet {
    private DnsHeader dnsHeader;
    private List<DnsQuestion> questions;
    private List<DnsAnswer> answers = new ArrayList<>();

    public DnsPacket(IP4Header ip4Header, UdpHeader udpHeader, ByteBuffer buffer) {
        super(ip4Header, udpHeader, buffer);
        ByteBuffer bufferDuplicated = buffer.duplicate();
        this.dnsHeader = new DnsHeader(bufferDuplicated);
        this.questions = new ArrayList<>();
        for (int i = 0; i < dnsHeader.getNQuestions(); i++) {
            questions.add(new DnsQuestion(bufferDuplicated));
        }
        // For now we wont map answers because we will do that programmatically
    }

    public DnsPacket(IP4Header ip4Header, UdpHeader udpHeader, DnsHeader dnsHeader) {
        super(ip4Header, udpHeader, ByteBufferPool.acquire());
        this.dnsHeader = dnsHeader;
        this.questions = new ArrayList<>();
    }

    public boolean isDNS() {
        return true;
    }

    public void addAnswer(String name, int type, int ttl, String data) {
        this.dnsHeader.addAnswer();
        this.answers.add(new DnsAnswer(name, type, ttl, data));
    }

    public DnsHeader getDnsHeader() {
        return this.dnsHeader;
    }

    public void addQuestion(String name, int type) {
        this.dnsHeader.addQuestion();
        this.questions.add(new DnsQuestion(name, type));
    }

    public List<DnsQuestion> getQuestions() {
        return questions;
    }

    public void fillBackingBuffer() {
        ByteBuffer buff = getBackingBuffer();
        int packetHeaderSize = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE;
        buff.position(packetHeaderSize);
        dnsHeader.putOn(buff);

        int firstAnswerNamePos = buff.position() - packetHeaderSize;

        questions.forEach(
                x -> x.putOn(buff)
        );

        for (int i = 0; i < answers.size(); i++) {
            DnsAnswer dnsAnswer = answers.get(i);
            firstAnswerNamePos = dnsAnswer.putOn(buff, firstAnswerNamePos) - packetHeaderSize;
        }

        buff.flip();
        buff.position(packetHeaderSize);
    }

    public List<DnsAnswer> getAnswers() {
        return this.answers;
    }

    @Override
    public String toString() {
        IP4Header ip4Header = super.getIp4Header();
        String superString = ip4Header != null ? super.getIp4Header().toString() : "";
        return superString + super.getHeader().toString() + "DnsPacket{" + "header=" + dnsHeader +
                ", questions=" + questions +
                ", answers=" + answers +
                '}';
    }
}

