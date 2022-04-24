package com.mocyx.basic_client.dns;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DnsPacket {
    private DnsHeader header;
    private List<DnsQuestion> questions;

    public DnsPacket(ByteBuffer buffer) {
        this.header = new DnsHeader(buffer);
        this.questions = new ArrayList<>();
        for (int i = 0; i < header.getNQuestions(); i++) {
            questions.add(new DnsQuestion(buffer));
        }
    }

    public DnsHeader getHeader() {
        return header;
    }

    public List<DnsQuestion> getQuestions() {
        return questions;
    }
}

