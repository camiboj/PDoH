package com.tpp.private_doh.dns;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RunWith(MockitoJUnitRunner.class)
public class PublicDnsRequesterTest {
    @Mock
    private SimpleResolver resolver;

    @InjectMocks
    private PublicDnsRequester publicDnsRequester;

    @Mock
    private CompletionStage<Message> messageCompletionStage;

    @Mock
    private Message message;

    @Test
    public void publicDnsResolverWorksOk() {
        String name = "name.com";
        int type = 1;

        when(messageCompletionStage.toCompletableFuture()).thenReturn(CompletableFuture.supplyAsync(() -> message));
        when(resolver.sendAsync(any())).thenReturn(messageCompletionStage);

        publicDnsRequester.executeRequest(name, type);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(resolver).sendAsync(captor.capture());

        Message queryMessage = captor.getValue();
        Record record = queryMessage.getQuestion();

        assertEquals(name + ".", record.getName().toString());
        assertEquals(type, record.getType());
    }
}
