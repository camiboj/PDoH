package com.tpp.private_doh.util;

import java.io.Closeable;
import java.io.IOException;

public class ResourceUtils {
    public static void closeResources(Closeable... resources) {
        for (Closeable resource : resources) {
            try {
                if (resource != null) {
                    resource.close();
                }
            } catch (IOException e) {
                // Ignore
            }
        }
    }

}
