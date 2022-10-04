package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.dns.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DnsToPublicDnsController implements DnsToController {
    private static final String TAG = DnsToPublicDnsController.class.getSimpleName();
    private final ShardingController shardingController;

    public DnsToPublicDnsController(ShardingController shardingController) {
        this.shardingController = shardingController;
    }

    public List<Response> process(DnsPacket dnsPacket) {
        List<DnsQuestion> questions = dnsPacket.getQuestions();
        return questions.stream().map(this::processQuestion).collect(Collectors.toList());
    }

    private Response processQuestion(DnsQuestion question) {
        Log.i(TAG, "Processing question");
        String name = question.getName() + "."; // This is a requirement of dns-java library
        try {
            List<CompletableFuture<Response>> requesters = shardingController.executeRequest(name, question.getType());
            CompletableFuture<Response>[] requestersArray = requesters.stream().toArray(CompletableFuture[]::new);
            Log.i(TAG, "Obtaining completable futures");
            return (Response) CompletableFuture.anyOf(requestersArray).get();
        } catch (Exception e) {
            Log.i(TAG, "Something bad happened while executing request", e);
            throw new RuntimeException("Something happened while processing DohRequest");
        }
    }
}
