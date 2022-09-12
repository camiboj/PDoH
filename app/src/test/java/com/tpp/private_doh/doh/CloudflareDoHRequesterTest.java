package com.tpp.private_doh.doh;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@RunWith(MockitoJUnitRunner.class)
public class CloudflareDoHRequesterTest extends DohHelper {
    private static String NAME = "someName";

    @Mock
    private URL url;

    @Mock
    private HttpURLConnection httpURLConnection;

    @Test
    public void testCloudflareDohFlow() throws IOException {
        when(url.openConnection()).thenReturn(httpURLConnection);
        String response = buildDohResponse();
        InputStream is = new ByteArrayInputStream(response.getBytes());
        when(httpURLConnection.getInputStream()).thenReturn(is);
        CloudflareDoHRequester cloudflareDoHRequester = new CloudflareDoHRequester(NAME, url);
        cloudflareDoHRequester.run();
        verify(httpURLConnection).setRequestMethod(any());
        verify(httpURLConnection).setRequestProperty("Accept", "application/dns-json");
        verifyDohResponse(cloudflareDoHRequester.getDohResponse());
    }

    @Test
    public void testCloudflareDohRequesterBuildsOk() {
        CloudflareDoHRequester cloudflareDoHRequester = new CloudflareDoHRequester(NAME);
        assertEquals("https://1.1.1.1/dns-query?name=someName", cloudflareDoHRequester.getUrl().toString());
    }
}
