package com.tpp.private_doh.factory;

import static org.junit.Assert.assertEquals;

import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.DoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.doh.Quad9DoHRequester;

import org.junit.Test;

public class DohRequesterFactoryTest {

    @Test
    public void testDohRequesterFactoryCreatesGoogleDohRequesterOk() {
        DoHRequester googleDohRequester = DohRequesterFactory.build(GoogleDoHRequester.class.getName());
        assertEquals(GoogleDoHRequester.class, googleDohRequester.getClass());
    }

    @Test
    public void testDohRequesterFactoryCreatesCloudflareDohRequesterOk() {
        DoHRequester cloudflareDohRequester = DohRequesterFactory.build(CloudflareDoHRequester.class.getName());
        assertEquals(CloudflareDoHRequester.class, cloudflareDohRequester.getClass());
    }

    @Test
    public void testDohRequesterFactoryCreatesQuad9DohRequesterOk() {
        DoHRequester quad9DohRequester = DohRequesterFactory.build(Quad9DoHRequester.class.getName());
        assertEquals(Quad9DoHRequester.class, quad9DohRequester.getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDohRequesterFactoryCreatesNonExistentRequesterAndFails() {
        DohRequesterFactory.build("Non existent requester");
    }
}
