package com.tpp.private_doh.controller;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.tpp.private_doh.config.Config;
import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.util.CombinationUtils;
import com.tpp.private_doh.util.Requester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PingController implements Runnable {
    private static final String TAG = PingController.class.getSimpleName();
    private final List<Requester> dnsRequesters;
    private int actualIdx;
    private LinkedBlockingQueue<String> activeIps;
    private List<List<String>> shardingGroups;
    private int nSharders;
    private boolean shouldRun;

    public PingController(int nSharders, List<String> requesters) {
        this.activeIps = new LinkedBlockingQueue<>();
        this.dnsRequesters = requesters.stream().map(PublicDnsRequester::new).collect(Collectors.toList());
        this.shardingGroups = new ArrayList<>();
        this.actualIdx = 0;
        this.nSharders = nSharders;
        this.shouldRun = true;
    }

    public List<Requester> getDnsRequesters() {
        return this.dnsRequesters;
    }

    public List<Requester> getActiveRequesters() {
        if (shardingGroups.isEmpty()) {
            return new ArrayList<>();
        }
        if (this.actualIdx == this.shardingGroups.size()) {
            this.actualIdx = 0;
        }
        int actualIdx = this.actualIdx;
        this.actualIdx += 1;
        List<String> ips = this.shardingGroups.get(actualIdx);

        return this.dnsRequesters.stream()
                .filter(requester -> ips.contains(requester.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public void run() {
        while (this.shouldRun) {
            for (int i = 0; i < this.dnsRequesters.size(); i++) {
                processIp(this.dnsRequesters.get(i));
                if (!this.shouldRun) {
                    break;
                }
            }
            try {
                Thread.sleep(Config.SLEEP_PING);
            } catch (InterruptedException e) {
                Log.e(TAG, "The thread was interrupted");
            }
        }

    }

    @VisibleForTesting
    public void processIp(Requester requester) {
        PublicDnsRequester publicDnsRequester = (PublicDnsRequester) requester;
        try {
            publicDnsRequester.executePingRequest(Config.PING_QUESTION, 1).get(Config.PING_TIMEOUT, TimeUnit.SECONDS);
            this.activeIps.add(publicDnsRequester.getIp());
        } catch (Exception e) {
            Log.i(TAG, String.format("Not active ip: %s", publicDnsRequester.getIp()));
            this.activeIps.remove(publicDnsRequester.getIp());
        }

        reprocessIps();
    }

    private void reprocessIps() {
        // Remove all groups that included a disabled ip
        shardingGroups.removeIf(shardingGroup ->
                shardingGroup.stream().noneMatch(requester -> this.activeIps.contains(requester)));

        // Create groups that include active ips
        List<List<String>> allGroups = CombinationUtils.combination(this.activeIps, this.nSharders);
        allGroups.forEach(group -> {
            if (!shardingGroups.contains(group)) {
                shardingGroups.add(group);
            }
        });

        Log.i(TAG, String.format("Actual idx: %d - Sharding groups: %s", actualIdx, shardingGroups));
    }

    public void stop() {
        Log.i(TAG, "Stopped pingController");
        this.shouldRun = false;
    }
}