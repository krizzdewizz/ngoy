package ngoy.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotated field's value or setter method will be injected right after the
 * component/class was instantiated but before {@link OnInit} will be called.
 * <p>
 * The annotated field must be public, non-final, non-static.<br>
 * The annotated setter method must be public, non-static and must have exactly
 * one parameter.<br>
 * Constructor parameters must not be annotated.
 * <p>
 * Example:
 * 
 * <pre>
 * &#64;Component(...)
 * public class MyComponent {
 * 	&#64;Inject
 * 	public MyService service;
 * 
 *	&#64;Inject
 * 	public void setOtherService(OtherService otherService) {
 * 		...
 * 	}
 * }
 * </pre>
 * <p>
 * You can use any annotation that is named <code>Inject</code>.
 * 
 * @author krizz
 * @see Optional
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
