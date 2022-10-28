package com.tpp.private_doh.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpp.private_doh.util.Requester;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class DnsShardingControllerTest {

    @Test
    public void testDnsShardingControllerWorksOk() {
        List<String> activeIps = Arrays.asList("1.1.1.1", "2.2.2.2");

        PingController pingController = mock(PingController.class);
        when(pingController.getActiveRequesters()).thenReturn(activeIps);

        DnsShardingController shardingController = new DnsShardingController(pingController);
        List<Requester> requesters = shardingController.getRequesters();
        assertEquals(2, requesters.size());
    }
}