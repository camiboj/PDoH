package com.tpp.private_doh.doh;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CloudflareDoHRequester extends DoHRequester {
    private static String ENDPOINT = "https://1.1.1.1/dns-query?";

    public CloudflareDoHRequester(String name) {
        super(name, ENDPOINT);
        HEADERS = new HashMap<String, List<String>>() {
            {
                put("Accept", Collections.singletonList("application/dns-json"));
            }
        };
    }
}
