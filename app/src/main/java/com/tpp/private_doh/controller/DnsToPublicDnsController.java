package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.util.Requester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DnsToPublicDnsController implements DnsToController {
    private static final String TAG = DnsToPublicDnsController.class.getSimpleName();
    private final ShardingController shardingController;

    public DnsToPublicDnsController() {
        List<Requester> requesters = new ArrayList<>();

        PublicDnsRequester publicDnsRequester = new PublicDnsRequester("8.8.8.8");
        PublicDnsRequester otherPublicDnsRequester = new PublicDnsRequester("1.1.1.1");
        requesters.add(publicDnsRequester);
        requesters.add(otherPublicDnsRequester);

        this.shardingController = new ShardingController(requesters, 2); // TODO: remove harcoded number
    }

    public List<Response> process(DnsPacket dnsPacket) {
        List<DnsQuestion> questions = dnsPacket.getQuestions();
        return questions.stream().map(this::processQuestion).collect(Collectors.toList());
    }

    private Response processQuestion(DnsQuestion question) {
        Log.i(TAG, "Processing question");
        String name = question.getName() + "."; // This is a requirement of dns-java library
        List<CompletableFuture<Response>> requesters = shardingController.executeRequest(name, question.getType());
        CompletableFuture<Response>[] requestersArray = requesters.stream().toArray(CompletableFuture[]::new);
        Log.i(TAG, "Obtaining completable futures");
        try {
            Object object = CompletableFuture.anyOf(requestersArray).get(100, TimeUnit.MILLISECONDS);
            if (object instanceof Response) {
                Log.i(TAG, "About to return response");
                return (Response) object;
            }
            Log.i(TAG, "Something bad happened while casting");
            return null;
        } catch (Exception e) {
            Log.i(TAG, "Something bad happened while executing request");
            throw new RuntimeException("Something happened while processing DohRequest");
        }
    }
}
