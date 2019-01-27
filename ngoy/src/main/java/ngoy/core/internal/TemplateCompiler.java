package ngoy.core.internal;

import ngoy.core.Nullable;

public interface TemplateCompiler {
	TemplateRender compile(@Nullable String template, Class<?> cmpClass);
}
