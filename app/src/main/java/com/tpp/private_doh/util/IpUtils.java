package com.tpp.private_doh.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpUtils {
    private static Integer TIMEOUT = 10;

    public static InetAddress getByName(String name) {
        try {
            return InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            throw new RuntimeException("The host is unreachable");
        }
    }

    public static InetAddress getByAddress(byte[] address) {
        try {
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            throw new RuntimeException("The host is unreachable");
        }
    }

    public static boolean isReachable(String pickedIp) {
        try {
            return IpUtils.getByName(pickedIp).isReachable(TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException(String.format("There was a network error while pinging %s", pickedIp), e);
        }
    }
}
