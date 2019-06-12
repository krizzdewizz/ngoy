package ngoy.internal.parser.template;

import java.io.StringWriter;

public class Printer {
    public static final Printer NULL_PRINTER = new Printer() {
        public void print(String text) {
        }

        @Override
        public String toString() {
            return "";
        }
    };

    private final StringWriter writer = new StringWriter();

    public void print(String text) {
        writer.write(text);
    }

    @Override
    public String toString() {
        return writer.toString();
    }
}