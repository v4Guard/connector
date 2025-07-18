package io.v4guard.connector.common.utils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;

public class HashCalculator {

    public static String calculateHash() {
        try {
            File jarFile = new File(HashCalculator.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            try (InputStream fis = Files.newInputStream(jarFile.toPath())) {
                int n = 0;
                byte[] buffer = new byte[8192];

                while (n != -1) {
                    n = fis.read(buffer);
                    if (n > 0) {
                        digest.update(buffer, 0, n);
                    }
                }

                byte[] messageDigest = digest.digest();
                StringBuilder hexString = new StringBuilder();

                for (byte aMessageDigest : messageDigest) {
                    String hex = Integer.toHexString(0xff & aMessageDigest);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }

                return hexString.toString();
            } catch (Exception ignored) {  }
        } catch (Exception ignored) {  }

        return "A40A8DE8192311FD425C4B53BB3C6FC4D595CF500D893AF0D93BFA5ACF2C235E";
    }
}
