package com.mocyx.basic_client.doh;

import java.util.HashMap;


public class Quad9DoHRequester extends DoHRequester {
    public Quad9DoHRequester(String name) {
        super(name);
        ENDPOINT = "https://9.9.9.9:5053/dns-query?";
        HEADERS = new HashMap<>();
    }
}
