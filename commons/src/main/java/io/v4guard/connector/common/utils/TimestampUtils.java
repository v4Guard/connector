package io.v4guard.connector.common.utils;

public class TimestampUtils {

    public static boolean isExpired(long current, long start, long needed) {
        return current - start >= needed;
    }

    public static boolean isExpired(long start,  long needed) {
        return isExpired(System.currentTimeMillis(), start, needed);
    }

}
