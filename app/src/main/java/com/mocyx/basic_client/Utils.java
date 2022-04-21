package com.mocyx.basic_client;

import java.io.Closeable;
import java.io.IOException;

public class Utils {

    public static void closeResources(Closeable... resources) {
        for (Closeable resource : resources) {
            try {
                resource.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
