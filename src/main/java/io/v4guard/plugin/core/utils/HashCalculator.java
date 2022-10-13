package io.v4guard.plugin.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class HashCalculator {

    public static String calculateHash(){
        try {
            File jarFile = new File(HashCalculator.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            InputStream fis = new FileInputStream(jarFile);
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
        } catch (Exception e) {
            return "7634670e00000000000000000000000000000001";
        }
    }



}
