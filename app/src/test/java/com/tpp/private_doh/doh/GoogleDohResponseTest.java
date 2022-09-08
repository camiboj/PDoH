package com.tpp.private_doh.doh;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.io.IOException;

public class GoogleDohResponseTest extends GoogleDohHelper {

    @Test
    public void testGoogleDohResponseBuiltOk() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String response = buildGoogleDohResponse();
        GoogleDohResponse googleDohResponse = mapper.readValue(response, GoogleDohResponse.class);
        verifyGoogleDohResponse(googleDohResponse);
    }
}
