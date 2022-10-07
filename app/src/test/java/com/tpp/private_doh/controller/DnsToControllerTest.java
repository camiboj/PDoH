package com.tpp.private_doh.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.dns.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RunWith(MockitoJUnitRunner.class)
public class DnsToControllerTest {
    @Mock
    DnsPacket dnsPacket;
    @Mock
    DnsQuestion dnsQuestion;
    @Mock
    CompletableFuture<Response> responseCompletableFuture;
    @Mock
    private ShardingController shardingController;
    @InjectMocks
    private DnsToController dnsToController;

    @Test
    public void testDnsToControllerWorksOk() {
        String name = "name";
        int type = 1;

        when(dnsQuestion.getName()).thenReturn(name);
        when(dnsQuestion.getType()).thenReturn(type);
        List<DnsQuestion> questions = Collections.singletonList(dnsQuestion);
        when(dnsPacket.getQuestions()).thenReturn(questions);
        when(shardingController.executeRequest(name, type)).thenReturn(Collections.singletonList(responseCompletableFuture));

        try {
            dnsToController.process(dnsPacket);
        } catch (Exception e) {
            // Expected exception
            verify(shardingController).executeRequest(name, type);
        }
    }
}
