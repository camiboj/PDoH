package com.tpp.private_doh.controller;

import android.util.Log;

import com.tpp.private_doh.constants.PublicDnsIps;
import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.util.CombinationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PingController implements Runnable {
    private static final String TAG = PingController.class.getSimpleName();
    private int actualIdx;
    private List<String> activeIps;
    private List<List<String>> shardingGroups;
    private int nSharders;

    public PingController() {
        this.activeIps = new ArrayList<>();
        this.shardingGroups = new ArrayList<>();
        this.actualIdx = 0;
    }

    public void setNSharders(int nSharders) {
        this.nSharders = nSharders;
    }

    public List<String> getActiveIps() {
        int actualIdx = this.actualIdx;
        this.actualIdx = ((actualIdx == (this.shardingGroups.size() - 1)) ? 0 : actualIdx + 1);
        return this.shardingGroups.get(actualIdx);
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
}
