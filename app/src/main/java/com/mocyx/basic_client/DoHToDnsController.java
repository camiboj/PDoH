package com.mocyx.basic_client;

import android.util.Log;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.doh.GoogleDohResponse;

import java.nio.ByteBuffer;

public class DoHToDnsController {
    private static final String TAG = "NetworkToDnsController";
    // create queue?

    public static void process(GoogleDohResponse dohResponse) {
        Log.i(TAG, String.format("dohResponse: %s", dohResponse));
        DnsPacket dns = new DnsPacket();

        dohResponse.getAnswers().forEach(
                x -> dns.addAnswer(x.getName(), x.getType(), x.getTtl(), x.getData())
        );
        dohResponse.getQuestions().forEach(
                x -> dns.addQuestion(x.getName(), x.getType())
        );


        Log.i(TAG, String.format("dns packet: %s", dns));
        ByteBuffer b = ByteBuffer.allocate(1000);
        dns.putOn(b);
    }
}
