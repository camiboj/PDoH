package com.mocyx.basic_client.util;

import java.io.Closeable;
import java.io.IOException;

public class ResourceUtils {
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
