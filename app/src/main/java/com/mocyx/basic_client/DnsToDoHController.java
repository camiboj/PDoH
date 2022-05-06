package com.mocyx.basic_client;

import android.util.Log;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.dns.DnsQuestion;
import com.mocyx.basic_client.doh.GoogleDoHRequester;

import java.util.List;

public class DnsToDoHController {
    private static final String TAG = "DnsToNetworkController";
    // TODO: create queue to avoid synchronic communication

    public static void process(DnsPacket dnsPacket) {
        List<DnsQuestion> questions = dnsPacket.getQuestions();
        Log.i(TAG, String.format("DNS header: %s", dnsPacket.getHeader()));
        Log.i(TAG, String.format("DNS questions: %s", dnsPacket.getQuestions()));

        questions.forEach(DnsToDoHController::processQuestion);

        // TODO: join threads and convert the answer from doh to dns packets
    }

    // private static GoogleDohResponse
    private static void processQuestion(DnsQuestion question) {
        GoogleDoHRequester googleDoH = new GoogleDoHRequester(question.getName());
        googleDoH.setType(question.getType());
        Thread t = new Thread(googleDoH);
        t.start();
        //return t.join
    }
}
