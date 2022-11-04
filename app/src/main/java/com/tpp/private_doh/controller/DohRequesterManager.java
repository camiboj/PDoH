package com.tpp.private_doh.controller;

import com.tpp.private_doh.util.CombinationUtils;
import com.tpp.private_doh.util.Requester;

import java.util.List;

public class DohRequesterManager {
    private List<List<Requester>> combinationRequesters;
    private List<Requester> requesters;
    private int actualIdx;

    public DohRequesterManager(List<Requester> requesters, int n) {
        this.combinationRequesters = CombinationUtils.combination(requesters, n);
        this.actualIdx = 0;
        this.requesters = requesters;
    }

    protected List<Requester> chooseRequesters() {
        int actualIdx = this.actualIdx;
        this.actualIdx = ((actualIdx == (this.combinationRequesters.size() - 1)) ? 0 : actualIdx + 1);
        return this.combinationRequesters.get(actualIdx);
    }

    public List<Requester> getRequesters() {
        return requesters;
    }
}
