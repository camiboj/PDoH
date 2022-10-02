package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.doh.Quad9DoHRequester;
import com.tpp.private_doh.util.Requester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DnsToDoHController implements DnsToController {
    private static final String TAG = DnsToDoHController.class.getSimpleName();
    private ShardingController shardingController;

    public DnsToDoHController() {
        List<Requester> requesters = new ArrayList<>();

        requesters.add(new GoogleDoHRequester());
        requesters.add(new CloudflareDoHRequester());
        requesters.add(new Quad9DoHRequester());

        this.shardingController = new ShardingController(requesters, 2);
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
            return (Response) CompletableFuture.anyOf(requestersArray).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Something happened while processing DohRequest");
        }
    }
}
