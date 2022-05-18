package com.mocyx.basic_client.dns;

import android.util.Log;

import com.mocyx.basic_client.util.BitUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


abstract class DnsName {

    // https://cabulous.medium.com/dns-message-how-to-read-query-and-response-message-cfebcb4fe817
    private final static String TAG = "DnsName";
    private final List<String> name;

    public DnsName(String name) {
        this.name = new ArrayList<>(Arrays.asList(name.split("\\."))); // TODO: split
        Log.i(TAG, String.format("name: %s", name));
    }

    public DnsName(ByteBuffer buffer) {
        name = new ArrayList<>();
        short labelLength = BitUtils.getUnsignedByte(buffer.get());
        while (labelLength > 0) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < labelLength; i++) { // TODO: investigate if its possible to get many chars at the same time
                char label = (char) buffer.get();
                sb.append(label);
            }
            name.add(sb.toString());
            labelLength = BitUtils.getUnsignedByte(buffer.get());
        }
    }

    public void putOn(ByteBuffer buf) { // is there a superclass method to override?
        name.forEach(x -> StringToBufferHelper.putOn(buf, x));
        buf.put((byte) 0);
    }

    public String toString() {
        return "Name {" + "name=" + name + '}';
    }

    public String join(String s) {
        return String.join(".", name);
    }

    static class StringToBufferHelper {
        private static void putOn(ByteBuffer buffer, String str) {
            buffer.put((byte) str.length());
            for (byte b : str.getBytes(StandardCharsets.UTF_8)) {
                buffer.put(b);
            }
        }
    }
}


