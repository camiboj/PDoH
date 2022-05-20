package com.mocyx.basic_client;

import android.util.Log;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.doh.GoogleDohResponse;

public class DoHToDnsMapper {
    private static final String TAG = DoHToDnsMapper.class.getSimpleName();;

    public static void map(GoogleDohResponse dohResponse, DnsPacket dnsPacket) {
        Log.i(TAG, String.format("dohResponse: %s", dohResponse));

        dohResponse.getAnswers().forEach(
                x -> dnsPacket.addAnswer(x.getName(), x.getType(), x.getTtl(), x.getData())
        );
        dohResponse.getQuestions().forEach(
                x -> dnsPacket.addQuestion(x.getName(), x.getType())
        );
    }
}
