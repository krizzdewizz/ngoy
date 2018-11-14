package org.ngoy.core.internal;

public class CmpRef {
	public final Class<?> clazz;
	public final String template;
	public final boolean directive;

	public CmpRef(Class<?> clazz, String template, boolean directive) {
		this.clazz = clazz;
		this.template = template;
		this.directive = directive;
	}
}