package io.v4guard.connector.common;

import java.util.logging.Logger;

public class UnifiedLogger {

    private static Logger logger;

    public static Logger get() {
        return logger;
    }

    public static void overrideBy(Logger newLogger) {
        logger = newLogger;
    }

}
