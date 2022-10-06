package com.tpp.private_doh.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.tpp.private_doh.util.Requester;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ShardingControllerTest {

    @Test
    public void testShardingControllerWorksOk() {
        Requester requester = mock(Requester.class);
        Requester otherRequester = mock(Requester.class);

        String name = "name";
        int type = 1;

        List<Requester> requesters = new ArrayList<>();
        requesters.add(requester);
        requesters.add(otherRequester);

        ShardingController shardingController = new ShardingController(requesters, 1);
        shardingController.executeRequest(name, type);

        verify(requester).executeRequest(name, type);
        verify(otherRequester, times(0)).executeRequest(name, type);

        shardingController.executeRequest(name, type);
        verifyNoMoreInteractions(requester);
        verify(otherRequester).executeRequest(name, type);
    }
}