package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.doh.GoogleDohResponse;

import java.util.List;
import java.util.stream.Collectors;

public class DnsToDoHController {
    private static final String TAG = DnsToDoHController.class.getSimpleName();
    ;

    public List<GoogleDohResponse> process(DnsPacket dnsPacket) {
        List<DnsQuestion> questions = dnsPacket.getQuestions();
        questions.forEach(this::processQuestion);

        List<GoogleDoHRequester> requesters = questions.stream().map(
                this::processQuestion
        ).collect(Collectors.toList());

        List<Thread> threads = requesters.stream().map(
                this::startThreads
        ).collect(Collectors.toList());

        threads.forEach(this::joinThreads);
        return requesters.stream().map(GoogleDoHRequester::getGoogleDohResponse).collect(Collectors.toList());
    }

    private Thread startThreads(GoogleDoHRequester requester) {
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

    private GoogleDoHRequester processQuestion(DnsQuestion question) {
        GoogleDoHRequester googleDoH = new GoogleDoHRequester(question.getName());
        googleDoH.setType(question.getType());
        return googleDoH;
    }
}
