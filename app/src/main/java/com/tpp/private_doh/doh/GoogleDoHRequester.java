package com.tpp.private_doh.doh;

import androidx.annotation.VisibleForTesting;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleDoHRequester extends DoHRequester {
    private static String ENDPOINT = "https://8.8.8.8/resolve?";
    private static Map<String, List<String>> HEADERS = new HashMap<String, List<String>>() {
        {
            put("Accept", Collections.singletonList("application/json"));
        }
    };

    public GoogleDoHRequester(String name) {
        super(name, ENDPOINT, HEADERS);
    }

    @VisibleForTesting
    public GoogleDoHRequester(String name, URL url) {
        super(name, ENDPOINT, HEADERS, url);
    }
}