package com.tpp.private_doh.util;

import org.xbill.DNS.Message;

import java.util.concurrent.CompletableFuture;

public interface Requester {
    CompletableFuture<Message> executeRequest(String name, int type);
}
