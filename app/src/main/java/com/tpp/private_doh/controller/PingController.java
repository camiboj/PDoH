package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.constants.PublicDnsIps;
import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.dns.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PingController implements Runnable {
    private static final String TAG = PingController.class.getSimpleName();

    private List<String> activeIps;

    public PingController() {
        this.activeIps = new ArrayList<>();
    }

    public List<String> getRandomActiveIps(int nIps) {
        List<String> randomActiveIps = new ArrayList<>();

        for (int i = 0; i < nIps; i++) {
            String activeIp = activeIps.get(new Random().nextInt(activeIps.size()));
            randomActiveIps.add(activeIp);
        }

        return randomActiveIps;
    }

    @Override
    public void run() {
        while (true) {
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
