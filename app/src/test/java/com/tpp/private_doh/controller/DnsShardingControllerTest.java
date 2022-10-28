package com.tpp.private_doh.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.util.Requester;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DnsShardingControllerTest {

    @Test
    public void testDnsShardingControllerWorksOk() {
        List<Requester> activeRequesters = Stream.of("1.1.1.1", "2.2.2.2").map(PublicDnsRequester::new).collect(Collectors.toList());

        PingController pingController = mock(PingController.class);
        when(pingController.getActiveRequesters()).thenReturn(activeRequesters);

        DnsShardingController shardingController = new DnsShardingController(pingController);
        List<Requester> requesters = shardingController.getRequesters();
        assertEquals(2, requesters.size());
    }
}