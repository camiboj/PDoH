package com.tpp.private_doh.dns;

import static com.tpp.private_doh.dns.DnsAnswer.ANSWER_CLASS;
import static com.tpp.private_doh.dns.DnsAnswerName.BEGINNING_MARK;
import static com.tpp.private_doh.dns.DnsQuestion.QUESTION_CLASS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpp.private_doh.protocol.IP4Header;
import com.tpp.private_doh.protocol.Packet;
import com.tpp.private_doh.protocol.UdpHeader;
import com.tpp.private_doh.util.ByteBufferPool;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.ByteBuffer;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DnsPacketTest {
    private static String NAME = "name";
    private static int TYPE = 1;
    private static int TTL = 1;
    private static String DATA = "121.122.123.124";
    private static short IDENTIFICATION = 1;
    private static short FLAGS = 2;
    private static short N_ADDITIONAL_RRS = 3;
    private static short N_QUESTIONS = 1;
    private static short N_ANSWERS = 1;
    private static short N_AUTHORITY_RESOURCE_RECORDS = 6;

    @Mock
    private IP4Header ip4Header;

    @Mock
    private UdpHeader udpHeader;

    @Mock
    private DnsHeader dnsHeader;

    @Test
    public void testDnsPacketBuildsOk() {
        when(udpHeader.isUDP()).thenReturn(true);
        when(udpHeader.isTCP()).thenReturn(false);
        DnsPacket dnsPacket = new DnsPacket(ip4Header, udpHeader, dnsHeader);
        assertTrue(dnsPacket.isDNS());
        assertTrue(dnsPacket.isUDP());
        assertFalse(dnsPacket.isTCP());
    }

    @Test
    public void testDnsPacketBuildsOkWithByteBuffer() {
        ByteBuffer buffer = buildBuffer();

        DnsPacket dnsPacket = new DnsPacket(ip4Header, udpHeader, buffer);
        assertEquals(1, dnsPacket.getQuestions().size());
    }

    @Test
    public void testDnsPacketAddAnswersOk() {
        DnsPacket dnsPacket = new DnsPacket(ip4Header, udpHeader, dnsHeader);
        dnsPacket.addAnswer(NAME, TYPE, TTL, DATA);

        verify(dnsHeader).addAnswer();

        List<DnsAnswer> answers = dnsPacket.getAnswers();
        assertEquals(1, answers.size());
    }

    @Test
    public void testDnsPacketAddQuestionsOk() {
        DnsPacket dnsPacket = new DnsPacket(ip4Header, udpHeader, dnsHeader);
        dnsPacket.addQuestion(NAME, TYPE);

        verify(dnsHeader).addQuestion();

        List<DnsQuestion> questions = dnsPacket.getQuestions();
        assertEquals(1, questions.size());
    }

    @Test
    public void fillBackingBufferOk() {
        DnsHeader dnsHeader = new DnsHeader(IDENTIFICATION, FLAGS, 0, 0,
                N_AUTHORITY_RESOURCE_RECORDS, N_ADDITIONAL_RRS);
        DnsPacket dnsPacket = new DnsPacket(ip4Header, udpHeader, dnsHeader);
        dnsPacket.addQuestion(NAME, TYPE);
        dnsPacket.addAnswer(NAME, TYPE, TTL, DATA);

        dnsPacket.fillBackingBuffer();
        ByteBuffer buffer = dnsPacket.getBackingBuffer();

        assertEquals(Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE, buffer.position());
        // DnsHeader
        assertEquals(IDENTIFICATION, buffer.getShort());
        assertEquals(FLAGS, buffer.getShort());
        assertEquals(N_QUESTIONS, buffer.getShort());
        assertEquals(N_ANSWERS, buffer.getShort());
        assertEquals(N_AUTHORITY_RESOURCE_RECORDS, buffer.getShort());
        assertEquals(N_ADDITIONAL_RRS, buffer.getShort());

        // DnsQuestion
        assertEquals((byte) NAME.length(), buffer.get());
        byte[] nameBytes = NAME.getBytes();
        for (int i = 0; i < NAME.length(); i++) {
            assertEquals(nameBytes[i], buffer.get());
        }
        assertEquals((byte) 0, buffer.get());
        assertEquals(TYPE, buffer.getShort());
        assertEquals(QUESTION_CLASS, buffer.getShort());

        // DnsAnswer
        assertEquals(BEGINNING_MARK, buffer.get());
        assertEquals((byte) 12, buffer.get());
        assertEquals(TYPE, buffer.getShort());
        assertEquals(ANSWER_CLASS, buffer.getShort());
        assertEquals(TTL, buffer.getInt());
        assertEquals(4, buffer.getShort()); // IPV4
        assertEquals((byte) 121, buffer.get());
        assertEquals((byte) 122, buffer.get());
        assertEquals((byte) 123, buffer.get());
        assertEquals((byte) 124, buffer.get());
    }

    private ByteBuffer buildBuffer() {
        ByteBuffer buffer = ByteBufferPool.acquire();
        buffer.putShort(IDENTIFICATION);
        buffer.putShort(FLAGS);
        buffer.putShort(N_QUESTIONS);
        buffer.putShort(N_ANSWERS);
        buffer.putShort(N_AUTHORITY_RESOURCE_RECORDS);
        buffer.putShort(N_ADDITIONAL_RRS);
        buffer.position(0);
        return buffer;
    }
}
