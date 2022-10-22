package com.tpp.private_doh.controller;

import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.util.Requester;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class ShardingController {

    abstract protected List<Requester> getRequesters();

    public List<CompletableFuture<Response>> executeRequest(String name, int type) {
        return getRequesters().stream()
                .map(requester -> requester.executeRequest(name, type))
                .collect(Collectors.toList());
    }
}
