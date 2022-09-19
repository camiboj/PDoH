package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.doh.DoHRequester;
import com.tpp.private_doh.doh.DohResponse;
import com.tpp.private_doh.doh.Quad9DoHRequester;

import java.util.List;
import java.util.stream.Collectors;

public class DnsToDoHController {
    private static final String TAG = DnsToDoHController.class.getSimpleName();
    ;

    public List<DohResponse> process(DnsPacket dnsPacket) {
        List<DnsQuestion> questions = dnsPacket.getQuestions();
        questions.forEach(this::processQuestion);

        List<DoHRequester> requesters = questions.stream().map(
                this::processQuestion
        ).collect(Collectors.toList());

        List<Thread> threads = requesters.stream().map(
                this::startThreads
        ).collect(Collectors.toList());

        threads.forEach(this::joinThreads);
        return requesters.stream().map(DoHRequester::getDohResponse).collect(Collectors.toList());
    }

    private Thread startThreads(DoHRequester requester) {
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

    private DoHRequester processQuestion(DnsQuestion question) {
        //GoogleDoHRequester googleDoH = new GoogleDoHRequester(question.getName());
        //CloudflareDoHRequester dohRequester = new CloudflareDoHRequester(question.getName());
        Quad9DoHRequester dohRequester = new Quad9DoHRequester(question.getName());
        dohRequester.setType(question.getType());
        return dohRequester;
    }
}
