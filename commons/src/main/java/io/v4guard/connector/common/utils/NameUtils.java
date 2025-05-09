package io.v4guard.connector.common.utils;

import java.io.File;

public class NameUtils {

    public static String getName() {
        if (System.getProperty("io.v4guard.connector.name") != null) {
            return System.getProperty("io.v4guard.connector.name");
        } else {
            return new File(System.getProperty("user.dir")).getName();
        }
    }
}
