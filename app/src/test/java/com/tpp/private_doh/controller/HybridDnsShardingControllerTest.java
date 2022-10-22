package com.tpp.private_doh.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tpp.private_doh.constants.PublicDnsIps;
import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.doh.Quad9DoHRequester;
import com.tpp.private_doh.util.Requester;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HybridDnsShardingControllerTest {

    @Test
    public void testHybridDnsShardingControllerWorksOkWithIps() {
        List<String> activeIps = Arrays.asList(PublicDnsIps.IPS.get(new Random().nextInt(PublicDnsIps.IPS.size())),
                PublicDnsIps.IPS.get(new Random().nextInt(PublicDnsIps.IPS.size())));

        PingController pingController = mock(PingController.class);
        when(pingController.getActiveIps()).thenReturn(activeIps);

        HybridDnsShardingController shardingController = new HybridDnsShardingController(pingController);
        List<Requester> requesters = shardingController.getRequesters();
        assertEquals(2, requesters.size());
    }

    @Test
    public void testHybridDnsShardingControllerWorksOkWithDohRequesters() {
        List<String> dohRequesters = Arrays.asList(GoogleDoHRequester.class.getName(),
                CloudflareDoHRequester.class.getName(), Quad9DoHRequester.class.getName());

        PingController pingController = mock(PingController.class);
        when(pingController.getActiveIps()).thenReturn(dohRequesters);

        HybridDnsShardingController shardingController = new HybridDnsShardingController(pingController);
        List<Requester> requesters = shardingController.getRequesters();
        assertEquals(3, requesters.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHybridDnsShardingControllerFails() {
        List<String> activeIps = Collections.singletonList("Non-existent ip");

        PingController pingController = mock(PingController.class);
        when(pingController.getActiveIps()).thenReturn(activeIps);

        HybridDnsShardingController shardingController = new HybridDnsShardingController(pingController);
        shardingController.getRequesters();
    }
}
