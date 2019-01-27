package ngoy.core.internal;

public interface TemplateCompiler {
	TemplateRender compile(String template, Class<?> cmpClass);
}
