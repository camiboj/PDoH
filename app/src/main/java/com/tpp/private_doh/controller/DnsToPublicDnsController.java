package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.mapper.DoHToDnsMapper;
import com.tpp.private_doh.mapper.PublicDnsToDnsMapper;

import org.xbill.DNS.Message;

import java.security.MessageDigest;
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
        //return shardingController.executeOtherRequest(name, question.getType());
        try {
            List<CompletableFuture<Message>> requesters = shardingController.executeRequest(name, question.getType());
            CompletableFuture<Message>[] requestersArray = requesters.stream().toArray(CompletableFuture[]::new);
            Log.i(TAG, "Obtaining completable futures");
            Object object = CompletableFuture.anyOf(requestersArray).get();
            if (object instanceof Message) {
                Log.i(TAG, "About to return response");
                return PublicDnsToDnsMapper.map((Message) object);
            }
            Log.i(TAG, String.format("Something bad happened while casting, %s", object.getClass()));
            return null;
        } catch (Exception e) {
            Log.i(TAG, "Something bad happened while executing request", e);
            throw new RuntimeException("Something happened while processing DohRequest");
        }
    }
}
