package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.doh.DohResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DnsToDoHController {
    private static final String TAG = DnsToDoHController.class.getSimpleName();
    private ShardingController shardingController;

    public DnsToDoHController(ShardingController shardingController) {
        this.shardingController = shardingController;
    }

    public List<DohResponse> process(DnsPacket dnsPacket) {
        Log.i(TAG, "Processing new package");
        List<DnsQuestion> questions = dnsPacket.getQuestions();
        return questions.stream().map(this::processQuestion).collect(Collectors.toList());
    }

    private DohResponse processQuestion(DnsQuestion question) {
        List<CompletableFuture<DohResponse>> requesters = shardingController.executeRequest(question.getName(), question.getType());
        CompletableFuture<DohResponse>[] requestersArray = requesters.stream().toArray(CompletableFuture[]::new);
        try {
            return (DohResponse) CompletableFuture.anyOf(requestersArray).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Something happened while processing DohRequest");
        }
    }
}
