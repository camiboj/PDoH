package com.mocyx.basic_client.doh;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

class ParameterStringBuilder {
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

public class GoogleDoH implements Runnable {
    // https://www.baeldung.com/java-http-request
    // https://www.baeldung.com/httpurlconnection-post
    private final static String TAG = "GoogleDoH";

    Map<String, String> parameters = new HashMap<>();

    public GoogleDoH(String name) {
        parameters.put("name", name);
    }

    public void setType(int type) {
        // Possible parameters https://developers.google.com/speed/public-dns/docs/doh/json
        // name, type, cd, ct, do, edns_client_subnet, random_padding
        parameters.put("type", Integer.toString(type));
    }

    private String getParameters() throws UnsupportedEncodingException {
        return ParameterStringBuilder.getParamsString(parameters);
    }

    private String getFinalEndpoint() throws UnsupportedEncodingException {
        String dohUrl = String.format("https://8.8.8.8/resolve?%s", getParameters());
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
            //   Set the Request Content-Type Header Parameter
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            //   Set Response Format Type
            con.setRequestProperty("Accept", "application/json");

            // Ensure the Connection Will Be Used to Send Content
            con.setDoOutput(true);

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
}

