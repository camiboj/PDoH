package com.tpp.private_doh.mapper;

import com.tpp.private_doh.dns.Response;

import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PublicDnsToDnsMapper {

    public static Response map(Message message) {
        Record apiQuestion = message.getQuestion();
        List<Response.Question> questions = new ArrayList<>();
        String apiName = message.getQuestion().getName().toString();
        apiName = apiName.substring(0, apiName.length() - 1);
        questions.add(new Response.Question(apiName, apiQuestion.getType()));

        List<Record> section = message.getSection(Section.ANSWER);
        List<Response.Answer> answers = section.stream().map(record -> {
            String recordName = record.getName().toString();
            recordName = recordName.substring(0, recordName.length() - 1);
            return new Response.Answer(recordName, record.getType(), (int) record.getTTL(), record.rdataToString());
        }).collect(Collectors.toList());

        return new Response(questions, answers);
    }
}
