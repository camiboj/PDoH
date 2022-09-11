package com.tpp.private_doh.dns;

import static org.junit.Assert.assertEquals;

import com.tpp.private_doh.util.ByteBufferPool;

import org.junit.Test;

import java.nio.ByteBuffer;

public class DnsHeaderTest {

    @Test
    public void testDnsHeaderBuildsOkWithByteBuffer() {
        short identification = 1;
        short flags = 2;
        short nAdditionalRRs = 3;
        short nQuestions = 4;
        short nAnswers = 5;
        short nAuthorityResourceRecords = 6;
        ByteBuffer buffer = ByteBufferPool.acquire();
        buffer.putShort(identification);
        buffer.putShort(flags);
        buffer.putShort(nQuestions);
        buffer.putShort(nAnswers);
        buffer.putShort(nAuthorityResourceRecords);
        buffer.putShort(nAdditionalRRs);

        // Reset
        buffer.position(0);

        DnsHeader dnsHeader = new DnsHeader(buffer);
        assertEquals(identification, dnsHeader.getIdentification());
        assertEquals(flags, dnsHeader.getFlags());
        assertEquals(nQuestions, dnsHeader.getNQuestions());
        assertEquals(nAnswers, dnsHeader.getNAnswers());
        assertEquals(nAuthorityResourceRecords, dnsHeader.getNAuthorityResourceRecords());
        assertEquals(nAdditionalRRs, dnsHeader.getNAdditionalRRs());
    }

    @Test
    public void testDnsHeaderBuildsAByteBufferOk() {
        short identification = 1;
        short flags = 2;
        short nAdditionalRRs = 3;
        short nQuestions = 4;
        short nAnswers = 5;
        short nAuthorityResourceRecords = 6;

        DnsHeader dnsHeader = new DnsHeader(identification, flags, nQuestions, nAnswers,
                nAuthorityResourceRecords, nAdditionalRRs);

        ByteBuffer buffer = ByteBufferPool.acquire();
        dnsHeader.putOn(buffer);

        //Reset
        buffer.position(0);

        assertEquals(identification, buffer.getShort());
        assertEquals(flags, buffer.getShort());
        assertEquals(nQuestions, buffer.getShort());
        assertEquals(nAnswers, buffer.getShort());
        assertEquals(nAuthorityResourceRecords, buffer.getShort());
        assertEquals(nAdditionalRRs, buffer.getShort());
    }
}
