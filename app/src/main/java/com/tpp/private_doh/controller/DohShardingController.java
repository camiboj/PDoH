package com.tpp.private_doh.controller;

import com.tpp.private_doh.util.Requester;

import java.util.List;

public class DohShardingController extends ShardingController {
    private static final String TAG = DohShardingController.class.getSimpleName();
    private final DohRequesterManager dohRequesterManager;

    public DohShardingController(DohRequesterManager dohRequesterManager) {
        super(dohRequesterManager.getRequesters());
        this.dohRequesterManager = dohRequesterManager;
    }

    @Override
    protected List<Requester> getRequesters() {
        return dohRequesterManager.chooseRequesters();
    }
}