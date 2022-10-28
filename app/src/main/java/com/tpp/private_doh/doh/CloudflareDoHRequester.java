package com.tpp.private_doh.doh;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudflareDoHRequester extends DoHRequester {
    private static String ENDPOINT = "https://1.1.1.1/dns-query?";
    private static Map<String, List<String>> HEADERS = new HashMap<String, List<String>>() {
        {
            put("Accept", Collections.singletonList("application/dns-json"));
        }
    };

    public CloudflareDoHRequester() {
        super(ENDPOINT, HEADERS);
    }

    @Override
    public String getName() {
        return CloudflareDoHRequester.class.getName();
    }
}
