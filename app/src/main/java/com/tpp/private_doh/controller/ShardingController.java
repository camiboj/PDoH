package com.tpp.private_doh.controller;

import com.tpp.private_doh.doh.Response;
import com.tpp.private_doh.util.CombinationUtils;
import com.tpp.private_doh.util.Requester;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ShardingController {
    private List<List<Requester>> requesters;
    private int actualIdx;
    private int nSharders;

    public ShardingController(List<Requester> requesters, int n) {
        this.requesters = CombinationUtils.combination(requesters, n);
        this.actualIdx = 0;
        this.nSharders = n;
    }

    public List<CompletableFuture<Response>> executeRequest(String name, int type) {
        List<Requester> requesters = this.requesters.get(actualIdx);
        actualIdx = actualIdx == (requesters.size() - 1) ? 0 : actualIdx + 1;
        return requesters.stream()
                .map(requester -> CompletableFuture.supplyAsync(() -> requester.executeRequest(name, type)))
                .collect(Collectors.toList());
    }
}
