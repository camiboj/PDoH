package com.tpp.private_doh.controller;

import com.tpp.private_doh.doh.DoHRequester;
import com.tpp.private_doh.util.CombinationUtils;

import java.util.List;

public class ShardingController {

    private List<List<DoHRequester>> doHRequesters;
    private int actualIdx;

    public ShardingController(List<DoHRequester> doHRequesters, int n) {
        this.doHRequesters = CombinationUtils.combination(doHRequesters, n);
        this.actualIdx = 0;
    }

    public DoHRequester executeRequest(String name, int type) {
        DoHRequester doHRequester = this.doHRequesters.get(actualIdx).get(0);
        doHRequester.executeRequest(name, type); // TODO: implement racing properly
        actualIdx = actualIdx == (doHRequesters.size() - 1) ? 0 : actualIdx + 1;
        return doHRequester;
    }
}
