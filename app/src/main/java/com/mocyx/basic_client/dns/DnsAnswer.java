package com.mocyx.basic_client.dns;

import android.util.Log;

import com.mocyx.basic_client.BitUtils;
import com.mocyx.basic_client.util.ByteBufferPool;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class DnsAnswer {
    // https://www.firewall.cx/networking-topics/protocols/domain-name-system-dns/161-protocols-dns-response.html
    private final static String TAG = "DnsAnswer";

    static class StringToBufferHelper {
        private static void putOn(ByteBuffer buffer,String str){
            Log.i(TAG,String.format("str: %s",str));
            buffer.put((byte)str.length());
            for(byte b:str.getBytes(StandardCharsets.UTF_8)){
                buffer.put(b);
            }
            Log.i(TAG,String.format("buffer: %s",buffer.array()));
        }
    }

    class Name {
        // https://cabulous.medium.com/dns-message-how-to-read-query-and-response-message-cfebcb4fe817
        private final byte END_QUESTION_NAME_MARK = (byte) 0x0;
        private final byte BEGINNING_MARK = (byte) 0xc0;
        private final List<String> name;

        public Name(String name) {
            this.name = new ArrayList<String>(Arrays.asList(name.split("\\."))); // TODO: split
            Log.i(TAG, String.format("name: %s", name));
            Log.i(TAG, String.format("this.name: %s", this.name));
        }

        public void putOn(ByteBuffer buf) { // is there a superclass method to override?
            // TODO: write lengths
            int offset = buf.position();
            name.forEach(x -> StringToBufferHelper.putOn(buf, x));
            buf.put(END_QUESTION_NAME_MARK);
            buf.put(BEGINNING_MARK);
            buf.putInt(offset);
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder("Name {");
            sb.append("name=").append(name);
            sb.append('}');
            return sb.toString();
        }
    }


    private final Name name;
    private final int type;

    // define a constant value that make sense. There is no class attr on Google DoH response
    private final int answClass = 1;

    private final int ttl; // define as time type object(?
    private final String data; // Address/CNAME

    public DnsAnswer (String name, int type, int ttl, String data) {
        this.name = new Name(name);
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
