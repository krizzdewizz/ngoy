package ngoy.core;

import java.lang.reflect.InvocationTargetException;

import static java.lang.String.format;

/**
 * Exception thrown from within ngoy.
 *
 * @author krizz
 */
public class NgoyException extends RuntimeException {

    public static Throwable realException(Throwable t) {
        if (t instanceof InvocationTargetException) {
            t = ((InvocationTargetException) t).getTargetException();
        }
        return t;
    }

    public static RuntimeException wrap(Throwable t) {
        t = realException(t);
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }

        return new NgoyException(t);
    }

    private static final long serialVersionUID = 1L;

    public NgoyException(String message, Object... args) {
        super(format(message, args));
    }

    public NgoyException(Throwable cause, String message, Object... args) {
        super(format(message, args), realException(cause));
    }

    protected NgoyException(Throwable cause) {
        super(realException(cause));
    }

}
