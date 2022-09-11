package com.tpp.private_doh.dns;

import androidx.annotation.VisibleForTesting;

import com.tpp.private_doh.util.BitUtils;

import java.net.InetAddress;
import java.nio.ByteBuffer;


public class DnsAnswer {
    // Class IN (most common)
    public static final int ANSWER_CLASS = 1;

    // https://www.firewall.cx/networking-topics/protocols/domain-name-system-dns/161-protocols-dns-response.html
    private final static String TAG = DnsAnswer.class.getSimpleName();

    private final DnsAnswerName name;
    private final int type;
    private final int ttl;
    private final String data;
    private int firstAnswerNamePos;

    public DnsAnswer(String name, int type, int ttl, String data) {
        this.name = new DnsAnswerName(name);
        this.type = type;
        this.ttl = ttl;
        this.data = data;
        this.firstAnswerNamePos = 0;
    }

    public int putOn(ByteBuffer buffer, int firstAnswerNamePos) {
        name.putOn(buffer, firstAnswerNamePos);
        buffer.putShort(BitUtils.intToShort(type));
        buffer.putShort(BitUtils.intToShort(ANSWER_CLASS));
        buffer.putInt(ttl);

        // Type 1 -> IP Address
        // Type 5 -> CNAMEs
        // TODO: map all types, for now I only saw type 1 and type 5 responses
        if (type == 1) {
            try {
                InetAddress ip = InetAddress.getByName(data);
                buffer.putShort(BitUtils.intToShort(4)); // IPV4
                this.firstAnswerNamePos = buffer.position();
                buffer.put(ip.getAddress());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (type == 5) {
            buffer.putShort(BitUtils.intToShort(data.length() + 1));
            DnsName dnsName = new DnsName(data);
            this.firstAnswerNamePos = buffer.position();
            dnsName.putOn(buffer);
        }

        return this.firstAnswerNamePos;
    }

    @VisibleForTesting
    public int getType() {
        return type;
    }

    @VisibleForTesting
    public int getTtl() {
        return ttl;
    }

    @VisibleForTesting
    public String getData() {
        return data;
    }

    public String toString() {
        return "GoogleDohAnswer {" + "name=" + name +
                ", type=" + type +
                ", ttl=" + ttl +
                ", data=" + data +
                '}';
    }
}
