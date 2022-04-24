package com.mocyx.basic_client;

import android.util.Log;

import com.mocyx.basic_client.dns.DnsPacket;
import com.mocyx.basic_client.doh.GoogleDohResponse;

import java.nio.ByteBuffer;

public class NetworkToDnsController {
    private static final String TAG = "NetworkToDnsController";
    // create queue?

    public static void process(GoogleDohResponse dohResponse) { // should it receive a DNS Packet? or a Packet?
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

        // Test works, but it does not test the answers because we don't process them when we read
        // from byte buffer (aka when localhost sends a dns packet to the network)
        b.position(0);
        DnsPacket dns_reread = new DnsPacket(b);
        Log.i(TAG, String.format("dns_reread packet: %s", dns_reread));

    }
}


// GoogleDohAnswer {name=Name {name=[graph, facebook, com]}, type=5, ttl=1, data=api.facebook.com.}
// GoogleDohAnswer {name=Name {name=[api, facebook, com]}, type=5, ttl=1, data=star.c10r.facebook.com.}
// GoogleDohAnswer {name=Name {name=[star, c10r, facebook, com]}, type=1, ttl=1, data=31.13.94.19}
//