package com.tpp.private_doh.mapper;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.doh.DohResponse;

import java.util.List;
import java.util.stream.Collectors;

public class DoHToDnsMapper {
    private static final String TAG = DoHToDnsMapper.class.getSimpleName();

    public static void map(Response response, DnsPacket dnsPacket) {
        Log.i(TAG, String.format("DohResponse: %s", response));

        response.getAnswers().forEach(
                x -> dnsPacket.addAnswer(x.getName(), x.getType(), x.getTtl(), x.getData())
        );
        response.getQuestions().forEach(
                x -> dnsPacket.addQuestion(x.getName(), x.getType())
        );
    }

    public static Response map(DohResponse dohResponse) {
        List<DohResponse.Question> dohQuestions = dohResponse.getQuestions();
        List<Response.Question> questions = dohQuestions.stream().map(dohQuestion ->
                new Response.Question(dohQuestion.getName(), dohQuestion.getType()))
                .collect(Collectors.toList());

        List<DohResponse.Answer> dohAnswers = dohResponse.getAnswers();
        List<Response.Answer> answers = dohAnswers.stream().map(dohAnswer ->
                new Response.Answer(dohAnswer.getName(), dohAnswer.getType(), dohAnswer.getTtl(),
                        dohAnswer.getData()))
                .collect(Collectors.toList());

        return new Response(questions, answers);
    }
}
