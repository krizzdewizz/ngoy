package ngoy.core;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotated parameter or method (return type) may have the null value.
 *
 * @author krizz
 */
@Documented
@Retention(RetentionPolicy.CLASS)
public @interface Nullable {
}
