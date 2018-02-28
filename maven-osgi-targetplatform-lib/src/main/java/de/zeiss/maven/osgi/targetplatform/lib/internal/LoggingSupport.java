package de.zeiss.maven.osgi.targetplatform.lib.internal;

import org.codehaus.plexus.logging.Logger;

/**
 * Provides access to the maven logger when started inside the plexus framework. Otherwise the output is printed normally.
 *
 */
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
