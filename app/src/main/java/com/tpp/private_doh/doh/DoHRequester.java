package com.tpp.private_doh.doh;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.mapper.DoHToDnsMapper;
import com.tpp.private_doh.util.Requester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public abstract class DoHRequester extends Requester {
    protected String TAG;

    private String endpoint;
    private Map<String, List<String>> headers;

    public DoHRequester(String endpoint, Map<String, List<String>> headers) {
        TAG = this.getClass().getSimpleName();
        this.endpoint = endpoint;
        this.headers = headers;
    }

    public CompletableFuture<Response> executeRequest(String name, int type) {
        return CompletableFuture.supplyAsync(() -> executeRequest(buildUrl(name, type))).thenApply((response) -> this.processResponse(response, System.nanoTime()));
    }

    @VisibleForTesting
    public Response executeRequest(URL url) {
        HttpURLConnection con = null;
        BufferedReader in = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setReadTimeout(1000);
            setHeaders(con);
            Log.i(TAG, String.format("Executing request: %s", con.getURL().toString()));
            Log.i(TAG, String.format("Status: %s", con.getResponseCode()));
            // Response
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            Log.i(TAG, String.format("Response: %s", response));
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            DohResponse dohResponse = mapper.readValue(response.toString(), DohResponse.class);
            Log.i(TAG, String.format("DohAnswer: %s", dohResponse));

            return DoHToDnsMapper.map(dohResponse);
        } catch (Exception e) {
            throw new RuntimeException("Something happened while executing request");
        }
        finally {
            if (con != null) {
                con.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException("Something happened while executing request");
                }
            }
        }
    }

    private String getFinalEndpoint(String name, int type) {
        try {
            Map<String, String> inputParameters = new HashMap<>();
            inputParameters.put("name", name);
            inputParameters.put("type", Integer.toString(type));
            String parameters = ParameterStringBuilder.getParamsString(inputParameters);
            return String.format("%s%s", this.endpoint, parameters);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("There was a problem building the endpoint");
        }
    }

    private URL buildUrl(String name, int type) {
        try {
            return new URL(getFinalEndpoint(name, type));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("There was a problem building the URL");
        }
    }

    private void setHeaders(HttpURLConnection con) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            entry.getValue().forEach(
                    value -> con.setRequestProperty(entry.getKey(), value)
            );
        }
    }

    static class ParameterStringBuilder {
        public static String getParamsString(Map<String, String> params)
                throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            return resultString.length() > 0
                    ? resultString.substring(0, resultString.length() - 1)
                    : resultString;
        }
    }
}

