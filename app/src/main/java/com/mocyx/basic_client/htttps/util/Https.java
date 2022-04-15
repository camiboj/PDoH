package com.mocyx.basic_client.htttps.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

public class Https implements Runnable {
    // https://www.baeldung.com/java-http-request

    private final URL url;

    public Https (String url) throws IOException { // "http://example.com", "GET"
        this.url = new URL(url);
    }

    @Override
    public void run() {
        HttpURLConnection con = null;
        BufferedReader in = null;
        try {
            con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");

            // HEADERS
            // https://www.baeldung.com/httpurlconnection-post
            //   Set the Request Content-Type Header Parameter
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            //   Set Response Format Type
            con.setRequestProperty("Accept", "application/json");

            // Ensure the Connection Will Be Used to Send Content
            con.setDoOutput(true);

            // PARAMETERS
            //Map<String, String> parameters = new HashMap<>();
            //parameters.put("name", "www.baeldung.com");
            //DataOutputStream out = new DataOutputStream(con.getOutputStream());
            //out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
            //out.flush();
            //out.close();
            //Log.i("TAG", String.format("[https] out: %s", ParameterStringBuilder.getParamsString(parameters)));

            // BODY (JSON)
            //String jsonInputString = "{\"name\": \"www.baeldung.com\"}";
            //// We would need to write it:
            //try(OutputStream os = con.getOutputStream()) {
            //    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            //    os.write(input, 0, input.length);
            //}

            // status
            //
            // int status = con.getResponseCode();
            // Reader streamReader = null;
            // if (status > 299) {
            //   streamReader = new InputStreamReader(con.getErrorStream());
            // } else {
            //   streamReader = new InputStreamReader(con.getInputStream());
            // }


            Log.i("TAG", String.format("[https] status: %s", con.getResponseCode()));

            // RESPONSE
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                Log.i("TAG", String.format("[https] response: %s", response));
                System.out.println(response);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}

