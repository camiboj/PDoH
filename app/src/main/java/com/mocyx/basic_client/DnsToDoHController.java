package com.mocyx.basic_client;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.dns.DnsQuestion;
import com.mocyx.basic_client.doh.GoogleDoHRequester;
import com.mocyx.basic_client.doh.GoogleDohResponse;

import java.util.List;
import java.util.stream.Collectors;

public class DnsToDoHController {
    private static final String TAG = DnsToDoHController.class.getSimpleName();
    ;

    public static List<GoogleDohResponse> process(DnsPacket dnsPacket) {
        List<DnsQuestion> questions = dnsPacket.getQuestions();
        questions.forEach(DnsToDoHController::processQuestion);

        List<GoogleDoHRequester> requesters = questions.stream().map(
                DnsToDoHController::processQuestion
        ).collect(Collectors.toList());

        List<Thread> threads = requesters.stream().map(
                DnsToDoHController::startThreads
        ).collect(Collectors.toList());

        threads.forEach(DnsToDoHController::joinThreads);
        return requesters.stream().map(GoogleDoHRequester::getGoogleDohResponse).collect(Collectors.toList());
    }

    private static Thread startThreads(GoogleDoHRequester requester) {
        Thread t = new Thread(requester);
        t.start();
        return t;
    }

    private static void joinThreads(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static GoogleDoHRequester processQuestion(DnsQuestion question) {
        GoogleDoHRequester googleDoH = new GoogleDoHRequester(question.getName());
        googleDoH.setType(question.getType());
        return googleDoH;
    }
}
