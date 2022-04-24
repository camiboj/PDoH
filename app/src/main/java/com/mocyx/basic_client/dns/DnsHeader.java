package com.mocyx.basic_client.dns;

import com.mocyx.basic_client.util.BitUtils;

import java.nio.ByteBuffer;

public class DnsHeader {
    private final int identification;
    private final int flags;
    private final int nAdditionalRRs;
    private int nQuestions;
    private int nAnswers;
    private int nAuthorityResourceRecords;

    public DnsHeader(ByteBuffer buffer) {
        this.identification = BitUtils.getUnsignedShort(buffer.getShort());
        this.flags = BitUtils.getUnsignedShort(buffer.getShort());
        this.nQuestions = BitUtils.getUnsignedShort(buffer.getShort());
        this.nAnswers = BitUtils.getUnsignedShort(buffer.getShort());
        this.nAuthorityResourceRecords = BitUtils.getUnsignedShort(buffer.getShort());
        this.nAdditionalRRs = BitUtils.getUnsignedShort(buffer.getShort());
    }

    public DnsHeader() {
        this.identification = 0;
        this.flags = 0;
        this.nQuestions = 0;
        this.nAnswers = 0;
        this.nAuthorityResourceRecords = 0;
        this.nAdditionalRRs = 0;
    }

    public void addAnswer() {
        nAnswers = nAnswers + 1;
    }

    public void addQuestion() {
        nQuestions = nQuestions + 1;
    }

    public void addAuthorityResourceRecord() {
        nAuthorityResourceRecords = nAuthorityResourceRecords + 1;
    }

    public int getNQuestions() {
        return nQuestions;
    }

    public int getNAnswers() {
        return nAnswers;
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

    public void putOn(ByteBuffer buff) {
        buff.putShort(BitUtils.intToShort(identification));
        buff.putShort(BitUtils.intToShort(flags));
        buff.putShort(BitUtils.intToShort(nQuestions));
        buff.putShort(BitUtils.intToShort(nAnswers));
        buff.putShort(BitUtils.intToShort(nAuthorityResourceRecords));
        buff.putShort(BitUtils.intToShort(nAdditionalRRs));
    }
}
