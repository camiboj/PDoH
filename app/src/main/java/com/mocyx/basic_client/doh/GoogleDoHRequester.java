package com.mocyx.basic_client.doh;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class GoogleDoHRequester extends DoHRequester {

    public GoogleDoHRequester(String name) {
        super(name);
        ENDPOINT = "https://8.8.8.8/resolve?";
        HEADERS = new HashMap<String, List<String>>() {
            {
                put("Accept", Collections.singletonList("application/json"));
            }};
    }
}