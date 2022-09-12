package com.tpp.private_doh.doh;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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


public abstract class DoHRequester implements Runnable {
    // https://www.baeldung.com/java-http-request
    // https://www.baeldung.com/httpurlconnection-post

    private final URL url;
    // TODO: Make mandatory for subclasses to override this attr
    protected String TAG;
    Map<String, String> parameters = new HashMap<>();
    private DohResponse dohResponse;
    private String endpoint;
    private Map<String, List<String>> headers;

    public DoHRequester(String name, String endpoint, Map<String, List<String>> headers) {
        TAG = this.getClass().getSimpleName();
        parameters.put("name", name);
        this.endpoint = endpoint;
        this.url = buildUrl();
        this.headers = headers;
    }

    @VisibleForTesting
    public DoHRequester(String name, String endpoint, Map<String, List<String>> headers, URL url) {
        TAG = this.getClass().getSimpleName();
        parameters.put("name", name);
        this.endpoint = endpoint;
        this.headers = headers;
        this.url = url;
    }

    public DohResponse getDohResponse() {
        return dohResponse;
    }

    public void setType(int type) {
        // Possible parameters https://developers.google.com/speed/public-dns/docs/doh/json
        parameters.put("type", Integer.toString(type));
    }

    @VisibleForTesting
    public URL getUrl() {
        return this.url;
    }

    @Override
    public void run() {
        HttpURLConnection con = null;
        BufferedReader in = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            setHeaders(con);
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

            dohResponse = mapper.readValue(response.toString(), DohResponse.class);
            Log.i(TAG, String.format("DohAnswer: %s", dohResponse));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.i("tag", "Fallo1");
                    e.printStackTrace();
                }
            }
        }
    }

    private String getParameters() throws UnsupportedEncodingException {
        return ParameterStringBuilder.getParamsString(parameters);
    }

    private String getFinalEndpoint() {
        try {
            String parameters = ParameterStringBuilder.getParamsString(this.parameters);
            return String.format("%s%s", this.endpoint, parameters);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("There was a problem building the endpoint");
        }
    }

    private URL buildUrl() {
        try {
            return new URL(getFinalEndpoint());
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

