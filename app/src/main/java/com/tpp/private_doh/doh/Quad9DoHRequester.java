package com.tpp.private_doh.doh;

import androidx.annotation.VisibleForTesting;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Quad9DoHRequester extends DoHRequester {
    private static String ENDPOINT = "https://9.9.9.9:5053/dns-query?";
    private static Map<String, List<String>> HEADERS = new HashMap<>();

    public Quad9DoHRequester() {
        super(ENDPOINT, HEADERS);
    }
}
