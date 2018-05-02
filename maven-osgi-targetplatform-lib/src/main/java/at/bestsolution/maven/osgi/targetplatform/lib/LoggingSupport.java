package at.bestsolution.maven.osgi.targetplatform.lib;

import org.codehaus.plexus.logging.Logger;

/**
 * Provides access to the maven logger when started inside the plexus framework. Otherwise the output is printed normally.
 *
 */
public class LoggingSupport {

    private static Logger LOGGER;

    public static void setLogger(Logger logger){
        LoggingSupport.LOGGER = logger;
    }
    
    
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

    public static void logInfoMessage(String message) {
        if (LOGGER != null) {
            LOGGER.info(message);
        } else {
            System.out.println(message);
        }
    }

    public static void logDebugMessage(String message) {
        if (LOGGER != null) {
            LOGGER.debug(message);
        } else {
            System.out.println(message);
        }
    }
}
