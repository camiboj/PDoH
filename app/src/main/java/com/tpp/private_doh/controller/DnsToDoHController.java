package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.doh.DoHRequester;
import com.tpp.private_doh.doh.DohResponse;
import com.tpp.private_doh.doh.Quad9DoHRequester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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

    private Thread startThreads(Runnable requester) {
        Thread t = new Thread(requester);
        t.start();
        return t;
    }

    private void joinThreads(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private DohResponse processQuestion(DnsQuestion question) {
        BlockingQueue<DohResponse> responses = new ArrayBlockingQueue<>(shardingController.getNSharders());
        List<Runnable> requesters = shardingController.executeRequest(question.getName(), question.getType(), responses);
        List<Thread> threads = requesters.stream().map(this::startThreads).collect(Collectors.toList());
        threads.forEach(this::joinThreads);
        return responses.peek(); // TODO: get element as soon as it is available. Maybe implement conditional variable algorithm
    }
}
