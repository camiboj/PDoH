package com.tpp.private_doh.doh;

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

    public GoogleDoHRequester() {
        super(ENDPOINT, HEADERS);
    }

    @Override
    public String getName() {
        return "Google Doh";
    }
}