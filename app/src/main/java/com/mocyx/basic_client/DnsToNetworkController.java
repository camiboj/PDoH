package com.mocyx.basic_client;

import android.util.Log;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.dns.DnsQuestion;
import com.mocyx.basic_client.htttps.doh.GoogleDoH;

import java.nio.ByteBuffer;
import java.util.List;

public class DnsToNetworkController {
    private static final String TAG = "GoogleDoHCONTROLLER";

    // TODO: create queue to avoid synchronic communication

    public static void process(ByteBuffer buffer) {
        DnsPacket dnsPacket = new DnsPacket(buffer);
        List<DnsQuestion> questions = dnsPacket.getQuestions();
        Log.i(TAG, String.format("DNS header: %s", dnsPacket.getHeader()));
        Log.i(TAG, String.format("DNS questions: %s", dnsPacket.getQuestions()));

        questions.forEach(DnsToNetworkController::processQuestion);

        // TODO: join threads and convert the answer from doh to dns packets

    }

    private static void processQuestion(DnsQuestion question) {
        GoogleDoH googleDoH = new GoogleDoH(question.getName());
        googleDoH.setType(question.getType());
        Thread t = new Thread(googleDoH);
        t.start();
    }
}
