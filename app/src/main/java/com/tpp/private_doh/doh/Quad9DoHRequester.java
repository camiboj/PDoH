package com.tpp.private_doh.doh;

import java.util.HashMap;


public class Quad9DoHRequester extends DoHRequester {
    private static String ENDPOINT = "https://9.9.9.9:5053/dns-query?";

    public Quad9DoHRequester(String name) {
        super(name, ENDPOINT);
        HEADERS = new HashMap<>();
    }
}
