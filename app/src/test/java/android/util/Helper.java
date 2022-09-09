package android.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Helper {

    protected InetAddress buildAddress(String name) {
        try {
            return InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed test");
        }
    }
}
