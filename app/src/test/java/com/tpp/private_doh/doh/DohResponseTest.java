package com.tpp.private_doh.doh;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.io.IOException;

public class DohResponseTest extends DohHelper {

    @Test
    public void testDohResponseBuiltOk() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String response = buildDohResponse();
        DohResponse dohResponse = mapper.readValue(response, DohResponse.class);
        verifyDohResponse(dohResponse);
    }
}
