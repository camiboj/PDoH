package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.dns.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DnsToController {
    private static final String TAG = DnsToController.class.getSimpleName();

    private final ShardingController shardingController;

    public DnsToController(ShardingController shardingController) {
        this.shardingController = shardingController;
    }

    public List<Response> process(DnsPacket dnsPacket) {
        Log.i(TAG, "Processing new package");
        List<DnsQuestion> questions = dnsPacket.getQuestions();
        return questions.stream().map(this::processQuestion).collect(Collectors.toList());
    }

    private Response processQuestion(DnsQuestion question) {
        List<CompletableFuture<Response>> requesters = shardingController.executeRequest(question.getName(), question.getType());
        CompletableFuture<Response>[] requestersArray = requesters.stream().toArray(CompletableFuture[]::new);
        try {
            Response response = (Response) CompletableFuture.anyOf(requestersArray).get();
            response.setAsWinner();
            return response;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Something happened while processing DohRequest");
        }
    }
}
