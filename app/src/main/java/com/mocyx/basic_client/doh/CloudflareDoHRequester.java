package com.mocyx.basic_client.doh;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class CloudflareDoHRequester extends DoHRequester {
    public CloudflareDoHRequester(String name) {
        super(name);
        ENDPOINT = "https://1.1.1.1/dns-query?";

        HEADERS = new HashMap<String, List<String>>() {
            {
                put("Accept", Collections.singletonList("application/dns-json"));
            }};
    }
}
