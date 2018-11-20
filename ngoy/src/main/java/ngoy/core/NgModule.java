package ngoy.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Organize declarations/providers and other modules together to be imported as
 * a whole.
 * <p>
 * As soon as your app consists of more than one component, a NgModule is needed
 * to make those components known to the engine. Ngoy could scan the classpath
 * for that, but it does <b>not</b> want to.
 * <p>
 * Unlike other module systems, there exists no boundaries between modules. At
 * the end, everything is stuffed into a single map. Everything can be reached
 * from anywhere.
 * 
 * @author krizz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NgModule {
	/**
	 * @return Components, directives and pipes that this module provides
	 */
	Class<?>[] declarations() default {};

	/**
	 * @return Other modules (classes annotated with {@link NgModule})
	 */
	Class<?>[] imports() default {};

	/**
	 * @return Providers/services
	 */
	Class<?>[] providers() default {};

	/**
	 * Declare another class for a provider/service.
	 * 
	 * @return Providers
	 */
	Provide[] provide() default {};
}
