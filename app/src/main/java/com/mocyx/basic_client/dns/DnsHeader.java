package com.mocyx.basic_client.dns;

import com.mocyx.basic_client.util.BitUtils;

import java.nio.ByteBuffer;

public class DnsHeader {
    private final int identification;
    private final int flags;
    private final int nQuestions;
    private final int nAnswers;
    private final int nAuthorityResourceRecords;
    private final int nAdditionalRRs;

    public DnsHeader(ByteBuffer buffer) {
        this.identification = BitUtils.getUnsignedShort(buffer.getShort());
        this.flags = BitUtils.getUnsignedShort(buffer.getShort());
        this.nQuestions = BitUtils.getUnsignedShort(buffer.getShort());
        this.nAnswers = BitUtils.getUnsignedShort(buffer.getShort());
        this.nAuthorityResourceRecords = BitUtils.getUnsignedShort(buffer.getShort());
        this.nAdditionalRRs = BitUtils.getUnsignedShort(buffer.getShort());
    }

    public int getNQuestions () {
        return nQuestions;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DnsHeader{");
        sb.append("identification=").append(identification);
        sb.append(", flags=").append(flags);
        sb.append(", nQuestions=").append(nQuestions);
        sb.append(", nAnswers=").append(nAnswers);
        sb.append(", nAuthorityResourceRecords=").append(nAuthorityResourceRecords);
        sb.append(", nAdditionalRRs=").append(nAdditionalRRs);
        sb.append('}');
        return sb.toString();
    }
}
