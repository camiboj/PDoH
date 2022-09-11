package com.tpp.private_doh.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.util.Helper;

import com.tpp.private_doh.dns.DnsAnswer;
import com.tpp.private_doh.dns.DnsHeader;
import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.dns.DnsQuestion;
import com.tpp.private_doh.doh.DohResponse;
import com.tpp.private_doh.protocol.IP4Header;
import com.tpp.private_doh.protocol.UdpHeader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@RunWith(MockitoJUnitRunner.class)
public class DnsControllerTest extends Helper {

    @Mock
    private DnsToDoHController dnsToDoHController;

    @Mock
    private IP4Header ip4Header;

    @Mock
    private UdpHeader udpHeader;

    @Mock
    private DnsHeader dnsHeader;

    @Mock
    private DohResponse googleDohResponse;

    @Mock
    private DohResponse.Answer answer;

    @Mock
    private DohResponse.Question question;

    @Test
    public void testDnsToDohControllerWorksOk() {
        // Ip4Header
        InetAddress sourceAddress = buildAddress("121.122.123.124");
        InetAddress destinationAddress = buildAddress("191.192.193.194");
        int identificationAndFlagsAndFragmentOffset = 1;
        when(ip4Header.getSourceAddress()).thenReturn(sourceAddress);
        when(ip4Header.getDestinationAddress()).thenReturn(destinationAddress);
        when(ip4Header.getIdentificationAndFlagsAndFragmentOffset())
                .thenReturn(identificationAndFlagsAndFragmentOffset);

        // UdpHeader
        int destinationPort = 1;
        int sourcePort = 2;
        when(udpHeader.getDestinationPort()).thenReturn(destinationPort);
        when(udpHeader.getSourcePort()).thenReturn(sourcePort);

        // DnsHeader
        int id = 1;
        int nAuthorityResourceRecords = 0;
        int nAdditionalRRs = 0;
        when(dnsHeader.getIdentification()).thenReturn(id);
        when(dnsHeader.getNAuthorityResourceRecords()).thenReturn(nAuthorityResourceRecords);
        when(dnsHeader.getNAdditionalRRs()).thenReturn(nAdditionalRRs);

        DnsPacket dnsPacket = new DnsPacket(ip4Header, udpHeader, dnsHeader);

        // Question
        String questionName = "someName";
        int questionType = 1;
        when(question.getName()).thenReturn(questionName);
        when(question.getType()).thenReturn(questionType);

        // Answer
        String answerData = "191.192.193.194";
        String answerName = "name";
        int ttl = 1;
        int answerType = 1;
        when(answer.getData()).thenReturn(answerData);
        when(answer.getName()).thenReturn(answerName);
        when(answer.getTtl()).thenReturn(ttl);
        when(answer.getType()).thenReturn(answerType);

        // DohResponse
        List<DohResponse.Answer> answers = new ArrayList<>();
        answers.add(answer);
        when(googleDohResponse.getAnswers()).thenReturn(answers);
        List<DohResponse.Question> questions = new ArrayList<>();
        questions.add(question);
        when(googleDohResponse.getQuestions()).thenReturn(questions);

        // DnsToDohController
        List<DohResponse> googleDohResponseList = new ArrayList<>();
        googleDohResponseList.add(googleDohResponse);
        when(dnsToDoHController.process(dnsPacket)).thenReturn(googleDohResponseList);

        BlockingQueue<DnsPacket> dnsPackets = new ArrayBlockingQueue<>(1000);
        DnsController dnsController = new DnsController(dnsPacket, dnsPackets, dnsToDoHController);

        dnsController.run();

        verify(dnsToDoHController).process(dnsPacket);
        assertEquals(1, dnsPackets.size());
        DnsPacket dnsPacketResult = dnsPackets.peek();
        assertNotNull(dnsPacketResult);

        // UdpHeader
        UdpHeader udpHeaderResult = (UdpHeader) dnsPacketResult.getHeader();
        assertEquals(sourcePort, udpHeaderResult.getDestinationPort());
        assertEquals(destinationPort, udpHeaderResult.getSourcePort());

        // DnsHeader
        DnsHeader dnsHeaderResult = dnsPacketResult.getDnsHeader();
        assertEquals(id, dnsHeaderResult.getIdentification());
        assertEquals(com.tpp.private_doh.protocol.IpUtil.FLAGS, dnsHeaderResult.getFlags());
        assertEquals(1, dnsHeaderResult.getNQuestions());
        assertEquals(1, dnsHeaderResult.getNAnswers());

        // DnsPacket
        List<DnsQuestion> questionsResult = dnsPacketResult.getQuestions();
        assertEquals(1, questionsResult.size());
        DnsQuestion dnsQuestion = questionsResult.get(0);
        assertEquals(questionType, dnsQuestion.getType());
        assertEquals(questionName, dnsQuestion.getName());

        List<DnsAnswer> answersResult = dnsPacketResult.getAnswers();
        assertEquals(1, answersResult.size());
        DnsAnswer dnsAnswer = answersResult.get(0);
        assertEquals(answerType, dnsAnswer.getType());
        assertEquals(answerData, dnsAnswer.getData());
        assertEquals(ttl, dnsAnswer.getTtl());
    }
}