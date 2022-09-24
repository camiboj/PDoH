package com.tpp.private_doh.doh;

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
public class Quad9DohRequesterTest extends DohHelper {
    @Mock
    private URL url;

    @Mock
    private HttpURLConnection httpURLConnection;

    @Test
    public void testQuad9DohFlow() throws IOException {
        when(url.openConnection()).thenReturn(httpURLConnection);
        String response = buildDohResponse();
        InputStream is = new ByteArrayInputStream(response.getBytes());
        when(httpURLConnection.getInputStream()).thenReturn(is);
        Quad9DoHRequester quad9DoHRequester = new Quad9DoHRequester();
        DohResponse dohResponse = quad9DoHRequester.executeRequest(url);
        verify(httpURLConnection).setRequestMethod(any());
        verifyDohResponse(dohResponse);
    }
}
