package com.tpp.private_doh.controller;

import com.tpp.private_doh.doh.DoHRequester;
import com.tpp.private_doh.doh.Response;
import com.tpp.private_doh.util.CombinationUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ShardingController {
    private List<List<DoHRequester>> doHRequesters;
    private int actualIdx;
    private int nSharders;

    public ShardingController(List<DoHRequester> doHRequesters, int n) {
        this.doHRequesters = CombinationUtils.combination(doHRequesters, n);
        this.actualIdx = 0;
        this.nSharders = n;
    }

    public List<CompletableFuture<Response>> executeRequest(String name, int type) {
        List<DoHRequester> doHRequesters = this.doHRequesters.get(actualIdx);
        actualIdx = actualIdx == (doHRequesters.size() - 1) ? 0 : actualIdx + 1;
        return doHRequesters.stream()
                .map(doHRequester -> CompletableFuture.supplyAsync(() -> doHRequester.executeRequest(name, type)))
                .collect(Collectors.toList());
    }

    public int getNSharders() {
        return nSharders;
    }
}
