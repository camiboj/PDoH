package com.tpp.private_doh.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tpp.private_doh.config.Config;
import com.tpp.private_doh.dns.PublicDnsRequester;
import com.tpp.private_doh.dns.Response;
import com.tpp.private_doh.util.Requester;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class PingControllerTest {

    @Mock
    public PublicDnsRequester publicDnsRequester;

    @Mock
    public Response response;

    @Test
    public void testPingControllerPingsActiveIp() {
        PingController pingController = new PingController(1);
        String ip = "1.1.1.1";

        when(publicDnsRequester.executePingRequest(Config.PING_QUESTION, 1)).thenReturn(CompletableFuture.supplyAsync(() -> response));
        when(publicDnsRequester.getIp()).thenReturn(ip);

        pingController.processIp(publicDnsRequester);

        List<String> activeIps = pingController.getActiveRequesters().stream().map(Requester::getName).collect(Collectors.toList());
        assertEquals(1, activeIps.size());
        assertEquals(ip, activeIps.get(0));
    }

    @Test
    public void testPingControllerPingNonActiveIpThatReturnsEmptyAnswer() throws ExecutionException, InterruptedException, TimeoutException {
        PingController pingController = new PingController(1);
        String ip = "1.1.1.1";
        CompletableFuture completableFuture = mock(CompletableFuture.class);

        when(publicDnsRequester.executePingRequest(Config.PING_QUESTION, 1)).thenReturn(completableFuture);
        when(completableFuture.get(Config.PING_TIMEOUT, TimeUnit.SECONDS)).thenThrow(new TimeoutException());
        when(publicDnsRequester.getIp()).thenReturn(ip);

        pingController.processIp(publicDnsRequester);

        List<Requester> activeRequesters = pingController.getActiveRequesters();
        assertTrue(activeRequesters.isEmpty());
    }

    @Test
    public void testPingControllerReturnsRightIp() {
        PingController pingController = new PingController(1);
        String ip = "1.1.1.1";
        String ip2 = "8.8.8.8";

        PublicDnsRequester publicDnsRequester2 = mock(PublicDnsRequester.class);

        when(publicDnsRequester.executePingRequest(Config.PING_QUESTION, 1)).thenReturn(CompletableFuture.supplyAsync(() -> response));
        when(publicDnsRequester.getIp()).thenReturn(ip);

        when(publicDnsRequester2.executePingRequest(Config.PING_QUESTION, 1)).thenReturn(CompletableFuture.supplyAsync(() -> response));
        when(publicDnsRequester2.getIp()).thenReturn(ip2);

        pingController.processIp(publicDnsRequester);

        List<String> activeIps = pingController.getActiveRequesters().stream().map(Requester::getName).collect(Collectors.toList());
        assertEquals(1, activeIps.size());
        assertEquals(ip, activeIps.get(0));

        pingController.processIp(publicDnsRequester2);

        List<String> activeIps2 = pingController.getActiveRequesters().stream().map(Requester::getName).collect(Collectors.toList());
        assertEquals(1, activeIps2.size());
        assertEquals(ip2, activeIps2.get(0));
    }
}