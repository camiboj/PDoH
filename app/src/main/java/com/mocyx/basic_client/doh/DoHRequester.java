package com.mocyx.basic_client.doh;

import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class DoHRequester implements Runnable {
    // https://www.baeldung.com/java-http-request
    // https://www.baeldung.com/httpurlconnection-post

    // TODO: Make mandatory for subclasses to override this attr
    protected String ENDPOINT;
    protected String TAG;
    protected Map<String, List<String>> HEADERS;

    Map<String, String> parameters = new HashMap<>();
    private DohResponse dohResponse;

    public DoHRequester(String name) {
        TAG = this.getClass().getSimpleName();
        parameters.put("name", name);
    }

    public DohResponse getGoogleDohResponse() {
        return dohResponse;
    }

    public void setType(int type) {
        // Possible parameters https://developers.google.com/speed/public-dns/docs/doh/json
        parameters.put("type", Integer.toString(type));
    }

    private String getParameters() throws UnsupportedEncodingException {
        return ParameterStringBuilder.getParamsString(parameters);
    }

    private String getFinalEndpoint() throws UnsupportedEncodingException {
        String dohUrl = String.format("%s%s", ENDPOINT, getParameters());
        Log.i(TAG, String.format("dohUrl: %s", dohUrl));
        return dohUrl;
    }

    @Override
    public void run() {
        HttpURLConnection con = null;
        BufferedReader in = null;
        try {
            URL url = new URL(getFinalEndpoint());
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            // HEADERS
            // TODO: each provider has its own headers as class attr
            setHeaders(con);

            Log.i(TAG, String.format("status: %s", con.getResponseCode()));

            // RESPONSE
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            Log.i(TAG, String.format("response: %s", response));
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            dohResponse = mapper.readValue(response.toString(), DohResponse.class);
            Log.i(TAG, String.format("googleDohAnswer: %s", dohResponse));
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

    private void setHeaders(HttpURLConnection con) {
        for (Map.Entry<String, List<String>> entry : HEADERS.entrySet())  {
            entry.getValue().forEach(
                    value -> con.setRequestProperty(entry.getKey(), value)
            );
        }
    };

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

