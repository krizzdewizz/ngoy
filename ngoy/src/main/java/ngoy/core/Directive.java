package ngoy.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Also known as a component without a template.
 * <p>
 * A directive allows to alter the attributes of a matching element with the use
 * of a {@link HostBinding} (like components do).
 * <p>
 * A directive may implement the {@link OnCompile} interface to alter the
 * template dynamically before the parser/compiler sees it (like components do).
 * <p>
 * A directive does not have a template and therefore does not alter the subtree
 * of it's matching elements.
 * 
 * @author krizz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Directive {
	/**
	 * The directive will be associated to all elements matching this CSS selector.
	 * 
	 * @return CSS selector
	 */
	String selector();

	/**
	 * Providers/services that this component depends on.
	 * 
	 * @return providers
	 */
	Class<?>[] providers() default {};

	/**
	 * Providers/services that this component depends on.
	 * 
	 * @return providers
	 */
	Provide[] provide() default {};
}
