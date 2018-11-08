package org.ngoy.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NgModule {
	Class<?>[] declarations() default {};

	Class<?>[] imports() default {};

	Class<?>[] providers() default {};

	Provide[] provide() default {};
}
