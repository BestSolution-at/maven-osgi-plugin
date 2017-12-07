package at.bestsolution.maven.osgi.support;

final class DebugSupport {

    /**
     * A list of colors which can be used in debug statements to style the output.
     */
    public enum TerminalOutputStyling {

        // Styling
        HIGH_INTENSITY("\u001B[1m"),
        LOW_INTENSITY("\u001B[2m"),
        ITALIC("\u001B[3m"),
        UNDERLINE("\u001B[4m"),
        BLINK("\u001B[5m"),
        RAPID_BLINK("\u001B[6m"),
        REVERSE_VIDEO("\u001B[7m"),
        INVISIBLE_TEXT("\u001B[8m"),

        // Colors

        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        MAGENTA("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m");

        /**
         * the end sequence to stop coloring
         */
        private final static String END_SEQUENCE = "\u001B[0m";

        private TerminalOutputStyling(String escSequenz) {
            this.escSequenz = escSequenz;
        }
        private String escSequenz;

        /**
         * Surrounds the given {@code content} with the this color.
         * @param content
         * @return
         */
        public String style(String content) {
            return escSequenz + content + END_SEQUENCE;
        }

    }
    

}
