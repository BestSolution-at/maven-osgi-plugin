package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import org.codehaus.plexus.logging.Logger;

public class LoggingSupport {

    public static Logger LOGGER;

    public static void logErrorMessage(String message, Throwable ex) {
        if (LOGGER != null) {
            LOGGER.error(message, ex);
        } else {
            System.out.println(message);
            ex.printStackTrace();
        }
    }

    public static void logErrorMessage(String message) {
        if (LOGGER != null) {
            LOGGER.error(message);
        } else {
            System.out.println(message);
        }
    }
}
