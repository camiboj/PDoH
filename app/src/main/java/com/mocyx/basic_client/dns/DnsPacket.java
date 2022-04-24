package com.mocyx.basic_client.dns;

import com.mocyx.basic_client.protocol.IP4Header;
import com.mocyx.basic_client.protocol.Packet;
import com.mocyx.basic_client.protocol.UdpHeader;

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
        final StringBuilder sb = new StringBuilder("DnsPacket{");
        sb.append("header=").append(header);
        sb.append(", questions=").append(questions);
        sb.append(", answers=").append(answers);
        sb.append('}');
        return sb.toString();
    }
}

