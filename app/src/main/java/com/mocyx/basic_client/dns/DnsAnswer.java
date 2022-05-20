package com.mocyx.basic_client.dns;

import com.mocyx.basic_client.util.BitUtils;

import java.nio.ByteBuffer;


public class DnsAnswer {
    // https://www.firewall.cx/networking-topics/protocols/domain-name-system-dns/161-protocols-dns-response.html
    private final static String TAG = DnsAnswer.class.getSimpleName();;

    private final DnsAnswerName name;
    private final int type;

    // define a constant value that make sense. There is no class attr on Google DoH response
    private final int answClass = 1;

    private final int ttl;
    private final String data;

    public DnsAnswer(String name, int type, int ttl, String data) {
        this.name = new DnsAnswerName(name);
        this.type = type;
        this.ttl = ttl;
        this.data = data;
    }

    public void putOn(ByteBuffer buffer) {
        name.putOn(buffer);
        buffer.putShort(BitUtils.intToShort(type));
        buffer.putShort(BitUtils.intToShort(answClass));
        buffer.putInt(ttl);
        buffer.putShort(BitUtils.intToShort(data.length()));
    }

    public String toString() {
        return "GoogleDohAnswer {" + "name=" + name +
                ", type=" + type +
                ", ttl=" + ttl +
                ", data=" + data +
                '}';
    }
}
