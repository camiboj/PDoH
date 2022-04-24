package com.mocyx.basic_client.dns;

import android.util.Log;

import com.mocyx.basic_client.BitUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class DnsAnswer {
    // https://www.firewall.cx/networking-topics/protocols/domain-name-system-dns/161-protocols-dns-response.html
    private final static String TAG = "DnsAnswer";

    private final DnsAnswerName name;
    private final int type;

    // define a constant value that make sense. There is no class attr on Google DoH response
    private final int answClass = 1;

    private final int ttl; // define as time type object(?
    private final String data; // Address/CNAME

    public DnsAnswer (String name, int type, int ttl, String data) {
        this.name = new DnsAnswerName(name);
        this.type = type;
        this.ttl = ttl;
        this.data = data;
    }

    public void putOn(ByteBuffer buffer) { // is there a superclass method to override?
        name.putOn(buffer);
        buffer.putShort(BitUtils.intToShort(type));
        buffer.putShort(BitUtils.intToShort(answClass));
        buffer.putInt(ttl);
        buffer.putShort(BitUtils.intToShort(data.length()));
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("GoogleDohAnswer {");
        sb.append("name=").append(name);
        sb.append(", type=").append(type);
        sb.append(", ttl=").append(ttl);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
