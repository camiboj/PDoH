package com.tpp.private_doh.doh;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.io.IOException;

public class ResponseTest extends DohHelper {

    @Test
    public void testDohResponseBuiltOk() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String response = buildDohResponse();
        Response dohResponse = mapper.readValue(response, Response.class);
        verifyDohResponse(dohResponse);
    }
}
