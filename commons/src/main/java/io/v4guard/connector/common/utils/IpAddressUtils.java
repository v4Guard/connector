package io.v4guard.connector.common.utils;

import java.util.regex.Pattern;

public class IpAddressUtils {

    private static final String ZERO_TO_255
            = "(\\d{1,2}|(0|1)\\"
            + "d{2}|2[0-4]\\d|25[0-5])";


    private static Pattern pattern
            = Pattern.compile(ZERO_TO_255 + "\\."
            + ZERO_TO_255 + "\\."
            + ZERO_TO_255 + "\\."
            + ZERO_TO_255);

    public static boolean isValidIPAddress(String ip) {
        if (ip == null) {
            return false;
        }
        return pattern.matcher(ip).matches();
    }
}
