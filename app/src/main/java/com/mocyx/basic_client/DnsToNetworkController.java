package com.mocyx.basic_client;

import android.util.Log;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.dns.DnsQuestion;
import com.mocyx.basic_client.htttps.util.GoogleDoH;

import java.nio.ByteBuffer;
import java.util.List;

public class DnsToNetworkController {
    private static final String TAG = "GoogleDoHCONTROLLER";
    // create queue?

    public static void process(ByteBuffer buffer) {
        DnsPacket dnsPacket = new DnsPacket(buffer);
        List<DnsQuestion> questions = dnsPacket.getQuestions();
        Log.i(TAG, String.format("DNS header: %s", dnsPacket.getHeader()));
        Log.i(TAG, String.format("DNS questions: %s", dnsPacket.getQuestions()));

        for (int i = 0; i < questions.size(); i++) {
            DnsQuestion question = questions.get(i);
            GoogleDoH googleDoH = new GoogleDoH(question.getName());
            googleDoH.setType(question.getType());
            // TODO: save `t` in queue (?
            Thread t = new Thread(googleDoH);
            t.start();
        }
        // TODO: join threads and convert the answer from doh to dns packets

    }
}
