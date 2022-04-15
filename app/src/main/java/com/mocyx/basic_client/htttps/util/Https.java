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
            Log.i("TAG", String.format("[https] con: %s", con));

            con.setRequestMethod("GET");

            // parameters
            //
            // Map<String, String> parameters = new HashMap<>();
            // parameters.put("param1", "val");
            // con.setDoOutput(true);
            // OutputStream outputStream = con.getOutputStream();
            // DataOutputStream out = new DataOutputStream(outputStream);
            // out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
            // out.flush();
            // out.close();
            // Log.i("TAG", String.format("[https] out: %s", out));

            // status
            //
            // int status = con.getResponseCode();
            // Reader streamReader = null;
            // if (status > 299) {
            //   streamReader = new InputStreamReader(con.getErrorStream());
            // } else {
            //   streamReader = new InputStreamReader(con.getInputStream());
            // }

            // headers
            //
            con.setRequestProperty("Content-Type", "application/json");

            Log.i("TAG", String.format("[https] status: %s", con.getResponseCode()));
            InputStream inputStream = con.getInputStream();
            Log.i("TAG", String.format("[https] inputStream: %s", inputStream));
            in = new BufferedReader(
                    new InputStreamReader(inputStream)
            );
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            Log.i("TAG", String.format("[https] content: %s", content));

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
                    e.printStackTrace();
                }
            }

        }
    }
}

