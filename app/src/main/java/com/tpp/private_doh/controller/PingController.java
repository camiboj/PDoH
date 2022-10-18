package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.app.Ping;
import com.tpp.private_doh.util.IpUtils;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PingController implements Runnable {
    private static final String TAG = PingController.class.getSimpleName();

    private Set<String> activeIps;

    public PingController() {
        this.activeIps = new HashSet<>();
    }

    public boolean isActive(String ip) {
        if (!activeIps.contains(ip)) {
            processIp(ip);
        } else {
            Log.i(TAG, String.format("Active ip %s", ip));
        }
        if (!activeIps.contains(ip)) {
            Log.i(TAG, String.format("Disabled ip %s", ip));
        } else {
            Log.i(TAG, String.format("Active ip %s", ip));
        }
        return activeIps.contains(ip);
    }

    @Override
    public void run() {
        while (true) {
            List<String> ipsToPing = Arrays.asList("208.67.222.222", "208.67.220.220", "1.1.1.1", "1.0.0.1", "8.8.8.8", "8.8.4.4", "9.9.9.9", "149.112.112.112");
            ipsToPing.forEach(this::processIp);
        }
    }

    private void processIp(String host) {
        final InetAddress dest = IpUtils.getByName(host);
        final Ping ping = new Ping(dest, new Ping.PingListener() {
            @Override
            public void onPing(final long timeMs, final int count) {
                Log.i(TAG, String.format("About to add: %s", host));
                activeIps.add(host);
            }

            @Override
            public void onPingException(final Exception e, final int count) {
                Log.i(TAG, String.format("About to delete: %s", host));
                activeIps.remove(host);
            }
        });
        ping.run();
    }
}
