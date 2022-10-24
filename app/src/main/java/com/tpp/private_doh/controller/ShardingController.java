package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.util.CombinationUtils;
import com.tpp.private_doh.util.Requester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ShardingController {
    private static final String TAG = ShardingController.class.getSimpleName();

    private List<List<Requester>> requestersCombinations;
    private List<Requester> requesters;
    private int actualIdx;

    public ShardingController(List<Requester> requesters, int n) {
        this.requesters = requesters;
        this.requestersCombinations = CombinationUtils.combination(requesters, n);
        this.actualIdx = 0;
    }

    public List<CompletableFuture<Response>> executeRequest(String name, int type) {
        List<Requester> requesters = this.requestersCombinations.get(actualIdx);
        this.actualIdx = ((actualIdx == (this.requestersCombinations.size() - 1)) ? 0 : actualIdx + 1);

        return requesters.stream()
                .map(requester -> requester.executeRequest(name, type))
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getRequestersMetrics() {
        Map<String, Integer> metrics = new HashMap<String, Integer>();

        for (Requester r : requesters) {
            metrics.put(r.getName(), r.getCount());
        }

        return metrics;
    }
}
