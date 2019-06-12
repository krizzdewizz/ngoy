package ngoy.internal.parser;

import static java.lang.String.format;

public class ForOfMicroParser {
    public static String parse(String s) {
        // *ngFor let x of all; index as i:<template content>
        String prefix = "*ngFor";
        if (!s.startsWith(prefix)) {
            return s;
        }

        String right = s.substring(prefix.length() + 1);
        int posEnd = right.indexOf(':');
        if (posEnd < 0) {
            throw new ParseException("forOf parse error: missing colon.");
        }

        String letPart = right.substring(0, posEnd);
        String rest = right.substring(posEnd + 1);

        return format("<ng-container *ngFor=\"%s\">%s</ng-container>", letPart, rest);
    }
}
