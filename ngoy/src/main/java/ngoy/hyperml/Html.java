package ngoy.hyperml;

import ngoy.hyperml.base.NgoyHtmlBase;

import java.io.OutputStream;
import java.io.Writer;

/**
 * HTML Elements and attributes, css.
 *
 * @author krizz
 */
public class Html extends NgoyHtmlBase<Html> {

    public static Html of() {
        return new Html();
    }

    public static Html to(Writer writer) {
        return new Html(writer);
    }

    public static Html to(OutputStream out) {
        return new Html(out);
    }

    public Html() {
    }

    public Html(Writer writer) {
        super(writer);
    }

    public Html(OutputStream out) {
        super(out);
    }
}
