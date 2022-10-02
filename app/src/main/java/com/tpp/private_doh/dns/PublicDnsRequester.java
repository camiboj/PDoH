package com.tpp.private_doh.dns;

import com.tpp.private_doh.util.Requester;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import java.util.ArrayList;
import java.util.List;

public class PublicDnsRequester implements Requester {
    @Override
    public Response executeRequest(String name, int type) {
        try {
            Record queryRecord = Record.newRecord(Name.fromString(name), Type.A, DClass.IN); // TODO: map class from type parameter to Type
            Message queryMessage = Message.newQuery(queryRecord);
            Resolver r = new SimpleResolver("8.8.8.8");
            Message message = r.sendAsync(queryMessage).toCompletableFuture().get();

            // TODO: move to mapper class
            Record apiQuestion = message.getQuestion();
            List<Response.Question> questions = new ArrayList<>();
            String apiName = message.getQuestion().getName().toString();
            apiName = apiName.substring(0, apiName.length() - 1);
            questions.add(new Response.Question(apiName, apiQuestion.getType()));

            List<Record> section = message.getSection(1);
            List<Response.Answer> answers = new ArrayList<>();
            section.forEach(record -> {
                String recordName = record.getName().toString();
                recordName = recordName.substring(0, recordName.length() - 1);
                answers.add(new Response.Answer(recordName, record.getType(), (int) record.getTTL(), record.rdataToString()));
            });

            return new Response(questions, answers);
        } catch (Exception e) {
            throw new RuntimeException("There was an error executing the request in DnsRequester", e);
        }
    }
}
