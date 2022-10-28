package com.tpp.private_doh.controller;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.tpp.private_doh.constants.PublicDnsIps;
import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.doh.Quad9DoHRequester;
import com.tpp.private_doh.util.CombinationUtils;
import com.tpp.private_doh.util.Requester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PingController implements Runnable {
    private static final String TAG = PingController.class.getSimpleName();
    private final List<Requester> dnsRequesters;
    private List<Requester> dohRequesters;
    private int actualIdx;
    private List<String> activeIps;
    private List<List<String>> shardingGroups;
    private int nSharders;

    public PingController() {
        this.activeIps = new ArrayList<>();
        this.dnsRequesters = PublicDnsIps.IPS.stream().map(PublicDnsRequester::new).collect(Collectors.toList());
        this.shardingGroups = new ArrayList<>();
        this.dohRequesters = new ArrayList<>();
        this.actualIdx = 0;
    }

    public List<Requester> getDnsRequesters() {
        return this.dnsRequesters;
    }

    public void setNSharders(int nSharders) {
        this.nSharders = nSharders;
    }

    public void addDohRequesters() {
        Log.i(TAG, "Someone called addDohRequesters");

        GoogleDoHRequester googleDoHRequester = new GoogleDoHRequester();
        CloudflareDoHRequester cloudflareDoHRequester = new CloudflareDoHRequester();
        Quad9DoHRequester quad9DoHRequester = new Quad9DoHRequester();

        this.dohRequesters = Arrays.asList(googleDoHRequester, cloudflareDoHRequester, quad9DoHRequester);
        this.activeIps.add(googleDoHRequester.getName());
        this.activeIps.add(cloudflareDoHRequester.getName());
        this.activeIps.add(quad9DoHRequester.getName());
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

        Stream<Requester> dnsRequesters = this.dnsRequesters.stream().filter(requester -> ips.contains(requester.getName()));
        Stream<Requester> dohRequesters = this.dohRequesters.stream().filter(requester -> ips.contains(requester.getName()));

        return Stream.concat(dnsRequesters, dohRequesters).collect(Collectors.toList());
    }

    @Override
    public void run() {
        while (true) {
            this.dnsRequesters.forEach(this::processIp);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @VisibleForTesting
    public void processIp(Requester requester) {
        PublicDnsRequester publicDnsRequester = (PublicDnsRequester) requester;
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
                Log.i(TAG, String.format("Active ips: %s", this.activeIps));
                Log.i(TAG, String.format("Group: %s", group));
                shardingGroups.add(group);
            }
        });

        Log.i(TAG, String.format("Actual idx: %d - Sharding groups: %s", actualIdx, shardingGroups));
    }
}