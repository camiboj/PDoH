package com.tpp.private_doh.controller;

import com.tpp.private_doh.util.CombinationUtils;
import com.tpp.private_doh.util.Requester;

import java.util.List;

public class DohShardingController extends ShardingController {
    private static final String TAG = DohShardingController.class.getSimpleName();

    private List<List<Requester>> requesters;
    private int actualIdx;

    public DohShardingController(List<Requester> requesters, int n) {
        this.requesters = CombinationUtils.combination(requesters, n);
        this.actualIdx = 0;
    }

    @Override
    protected List<Requester> getRequesters() {
        int actualIdx = this.actualIdx;
        this.actualIdx = ((actualIdx == (this.requesters.size() - 1)) ? 0 : actualIdx + 1);
        return this.requesters.get(actualIdx);
    }
}