package com.mocyx.basic_client.dns;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DnsPacket {
    private DnsHeader header;
    private List<DnsQuestion> questions = new ArrayList<DnsQuestion>();
    private List<DnsAnswer> answers = new ArrayList<DnsAnswer>();

    public DnsPacket(ByteBuffer buffer) {
        this.header = new DnsHeader(buffer);
        for (int i = 0; i < header.getNQuestions(); i++) {
            questions.add(new DnsQuestion(buffer));
        }
    }

    public DnsPacket() {
        this.header = new DnsHeader();
        this.questions = new ArrayList<DnsQuestion>();
    }

    public void addAnswer(String name, int type, int ttl, String data) {
        this.header.addAnswer();
        this.answers.add(new DnsAnswer(name, type, ttl, data));
    }

    public void addQuestion(String name, int type) {
        this.header.addQuestion();
        this.questions.add(new DnsQuestion(name, type));
    }

    public DnsHeader getHeader() {
        return header;
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

