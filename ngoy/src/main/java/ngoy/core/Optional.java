package ngoy.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a service/provider for an {@link Inject} annotated field is not available,
 * do nothing instead of throwing a 'provider not found' exception.
 * 
 * @author krizz
 * @see Inject
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Optional {
}
