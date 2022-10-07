package com.tpp.private_doh.mapper;


import static org.junit.Assert.assertEquals;

import com.tpp.private_doh.dns.Response;

import org.junit.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TextParseException;

import java.util.List;

public class PublicDnsToDnsMapperTest {

    @Test
    public void testMapperWorksOk() throws TextParseException {
        String name = "name.";
        String answerName = "answerName.";
        int type = 1;
        int ttl = 64;
        byte[] data = new byte[4];
        for (int i = 0; i < 4; i++) {
            data[0] = (byte) 114;
            data[1] = (byte) 114;
            data[2] = (byte) 114;
            data[3] = (byte) 114;
        }
        Record queryRecord = Record.newRecord(Name.fromString(name), type, DClass.IN);
        Record answerRecord = Record.newRecord(Name.fromString(answerName), type, DClass.IN, ttl, data);
        Message message = new Message();
        message.addRecord(queryRecord, Section.QUESTION);
        message.addRecord(answerRecord, Section.ANSWER);

        Response response = PublicDnsToDnsMapper.map(message);

        List<Response.Question> questions = response.getQuestions();
        List<Response.Answer> answers = response.getAnswers();

        assertEquals(1, questions.size());
        Response.Question question = questions.get(0);
        assertEquals("name", question.getName());
        assertEquals(type, question.getType());

        assertEquals(1, answers.size());
        Response.Answer answer = answers.get(0);
        assertEquals("answerName", answer.getName());
        assertEquals(type, answer.getType());
        assertEquals(ttl, answer.getTtl());
        assertEquals("114.114.114.114", answer.getData());
    }
}
