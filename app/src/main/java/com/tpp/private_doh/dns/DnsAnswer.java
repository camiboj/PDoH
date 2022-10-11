package com.tpp.private_doh.dns;

import androidx.annotation.VisibleForTesting;

import com.tpp.private_doh.util.BitUtils;
import com.tpp.private_doh.util.IpUtils;

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

        if (type == 1) { // Ipv4 - Record A
            putIp(4, buffer);
        }
        if (type == 28) { //Ipv6 - Record AAAA
            putIp(16, buffer);
        }
        if (type == 5) { // Record CNAME
            buffer.putShort(BitUtils.intToShort(data.length() + 1));
            DnsName dnsName = new DnsName(data);
            this.firstAnswerNamePos = buffer.position();
            dnsName.putOn(buffer);
        }

        return this.firstAnswerNamePos;
    }

    private void putIp(int bytes, ByteBuffer buffer) {
        try {
            InetAddress ip = IpUtils.getByName(data);
            buffer.putShort(BitUtils.intToShort(bytes));
            this.firstAnswerNamePos = buffer.position();
            buffer.put(ip.getAddress());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        return "DohAnswer {" + "name=" + name +
                ", type=" + type +
                ", ttl=" + ttl +
                ", data=" + data +
                '}';
    }
}
