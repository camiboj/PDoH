package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.util.Requester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class ShardingController {
    private List<Requester> requesters;

    public ShardingController(List<Requester> requesters) {
        this.requesters = requesters;
    }

    abstract protected List<Requester> getRequesters();

    public List<CompletableFuture<Response>> executeRequest(String name, int type) {
        return getRequesters().stream()
                .map(requester -> requester.executeRequest(name, type))
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getRequestersMetrics() {
        Map<String, Integer> metrics = new HashMap<>();

        for (Requester r : requesters) {
            metrics.put(r.getName(), r.getCount());
        }
        return metrics;
    }
}