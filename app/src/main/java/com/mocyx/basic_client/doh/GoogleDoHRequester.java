package com.mocyx.basic_client.doh;

import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocyx.basic_client.DoHToDnsController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GoogleDoHRequester extends DoHRequester {

    public GoogleDoHRequester(String name) {
        super(name);
        ENDPOINT = "https://8.8.8.8/resolve?";
        TAG = "GoogleDoH";
        HEADERS = new HashMap<String, List<String>>() {
            {
                put("Accept", Collections.singletonList("application/json"));
            }};
    }
}