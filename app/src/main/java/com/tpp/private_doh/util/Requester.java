package com.tpp.private_doh.util;

import com.tpp.private_doh.dns.Response;

import java.util.concurrent.CompletableFuture;

public interface Requester {
    CompletableFuture<Response> executeRequest(String name, int type);

    int getCount();
}
