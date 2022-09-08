package com.tpp.private_doh.util;

import android.util.Log;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.doh.GoogleDohResponse;

public class DoHToDnsMapper {
    private static final String TAG = DoHToDnsMapper.class.getSimpleName();

    public static void map(GoogleDohResponse dohResponse, DnsPacket dnsPacket) {
        Log.i(TAG, String.format("DohResponse: %s", dohResponse));

        dohResponse.getAnswers().forEach(
                x -> dnsPacket.addAnswer(x.getName(), x.getType(), x.getTtl(), x.getData())
        );
        dohResponse.getQuestions().forEach(
                x -> dnsPacket.addQuestion(x.getName(), x.getType())
        );
    }
}
