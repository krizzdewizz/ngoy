package org.ngoy.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
	String selector();

	String templateUrl() default "";

	String template() default "";

	String contentType() default "";

	String[] styleUrls() default {};

	Class<?>[] declarations() default {};

	Class<?>[] providers() default {};

	Provide[] provide() default {};
}
