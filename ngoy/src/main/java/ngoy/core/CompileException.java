package ngoy.core;

/**
 * Exception thrown while compiling the template to Java.
 *
 * @author krizz
 */
public class CompileException extends NgoyException {

    private static final long serialVersionUID = 1L;

    public CompileException(String message, Object... args) {
        super(message, args);
    }
}
