package com.tpp.private_doh.doh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
public class GoogleDoHRequesterTest extends DohHelper {
    private static String NAME = "someName";

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
        GoogleDoHRequester googleDoHRequester = new GoogleDoHRequester(NAME, url);
        googleDoHRequester.run();
        verify(httpURLConnection).setRequestMethod(any());
        verify(httpURLConnection).setRequestProperty("Accept", "application/json");
        verifyDohResponse(googleDoHRequester.getGoogleDohResponse());
    }

    @Test
    public void testGoogleDohRequesterBuildsOk() {
        GoogleDoHRequester googleDoHRequester = new GoogleDoHRequester(NAME);
        assertEquals("https://8.8.8.8/resolve?name=someName", googleDoHRequester.getUrl().toString());
    }
}
