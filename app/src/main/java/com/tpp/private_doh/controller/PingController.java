package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.constants.PublicDnsIps;
import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.dns.Response;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PingController implements Runnable {
    private static final String TAG = PingController.class.getSimpleName();

    private Set<String> activeIps;

    public PingController() {
        this.activeIps = new HashSet<>();
    }

    public boolean isActive(String ip) {
        return activeIps.contains(ip);
    }

    @Override
    public void run() {
        while (true) {
            List<String> strings = Arrays.asList("208.67.222.222", "208.67.220.220", "1.1.1.1", "1.0.0.1", "8.8.8.8", "8.8.4.4", "9.9.9.9", "149.112.112.112");
            PublicDnsIps.IPS.stream().map(PublicDnsRequester::new).forEach(this::processIp);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processIp(PublicDnsRequester publicDnsRequester) {
        try {
            Response ping = publicDnsRequester.executeRequestWithoutSentinel("google.com", 1).get(30, TimeUnit.SECONDS);

            if (!ping.getAnswers().isEmpty()) {
                Log.i(TAG, String.format("Active ip: %s", publicDnsRequester.getIp()));
                this.activeIps.add(publicDnsRequester.getIp());
            } else {
                this.activeIps.remove(publicDnsRequester.getIp());
            }
        } catch (Exception e) {
            Log.i(TAG, String.format("Not active ip: %s", publicDnsRequester.getIp()));
            this.activeIps.remove(publicDnsRequester.getIp());
        }
    }
}
