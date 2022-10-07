package com.tpp.private_doh.doh;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpp.private_doh.dns.Response;

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
public class GoogleDoHRequesterTest extends DohHelper {
    @Mock
    private URL url;

    @Mock
    private HttpURLConnection httpURLConnection;

    @Test
    public void testGoogleDohFlow() throws IOException {
        when(url.openConnection()).thenReturn(httpURLConnection);
        String response = buildDohResponse();
        InputStream is = new ByteArrayInputStream(response.getBytes());
        when(httpURLConnection.getInputStream()).thenReturn(is);
        GoogleDoHRequester googleDoHRequester = new GoogleDoHRequester();
        Response dohResponse = googleDoHRequester.executeRequest(url);
        verify(httpURLConnection).setRequestMethod(any());
        verify(httpURLConnection).setRequestProperty("Accept", "application/json");
        verifyDohResponse(dohResponse);
    }
}
