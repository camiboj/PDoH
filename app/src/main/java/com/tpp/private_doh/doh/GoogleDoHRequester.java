package com.tpp.private_doh.doh;

import androidx.annotation.VisibleForTesting;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GoogleDoHRequester extends DoHRequester {
    private static String ENDPOINT = "https://8.8.8.8/resolve?";

    public GoogleDoHRequester(String name) {
        super(name, ENDPOINT);
        HEADERS = new HashMap<String, List<String>>() {
            {
                put("Accept", Collections.singletonList("application/json"));
            }
        };
    }

    @VisibleForTesting
    public GoogleDoHRequester(String name, URL url) {
        super(name, ENDPOINT, url);
        HEADERS = new HashMap<String, List<String>>() {
            {
                put("Accept", Collections.singletonList("application/json"));
            }
        };
    }
}