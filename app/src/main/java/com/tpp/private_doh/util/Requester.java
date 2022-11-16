package com.tpp.private_doh.util;

import com.tpp.private_doh.dns.RTT;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.mapper.PublicDnsToDnsMapper;

import org.xbill.DNS.Message;

import java.util.concurrent.CompletableFuture;


public abstract class Requester {
    private int count;
    private RTT avgResponseTime = new RTT();

    public CompletableFuture<Response> executeRequest(String name, int type) {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        throw new UnsupportedOperationException();
    }

    public int getCount() {
        return count;
    }

    public RTT getAvgResponseTime() {
        return avgResponseTime;
    }

    private void increaseCount() {
        count ++;
    }

    private void updateAvgResponseTime(long new_measure) {
        avgResponseTime.update(new_measure);
    }

    protected Response processMessage(Message message, long rtt_0) {
        Response r = PublicDnsToDnsMapper.map(message);
        processResponse(r, rtt_0);
        return r;
    }
    protected Response processResponse(Response response, long rtt_0) {
        response.setOnWinning(() -> {
            this.increaseCount();
            this.updateAvgResponseTime(rtt_0 - System.currentTimeMillis());
        });
        return response;
    }
}
