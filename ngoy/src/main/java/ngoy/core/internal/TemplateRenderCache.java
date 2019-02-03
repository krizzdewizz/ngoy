package ngoy.core.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Caches a component's input info.
 * 
 * @author krizz
 */
public enum TemplateRenderCache {
	INSTANCE;

	private final Map<Class<?>, TemplateRender> map = new HashMap<>();

	public TemplateRender compile(Class<?> clazz, TemplateCompiler compiler) {
		return getTemplateRender(clazz, compiler::compile);
	}

	public TemplateRender getTemplateRender(Class<?> clazz, Function<Class<?>, TemplateRender> producer) {
		TemplateRender render = map.get(clazz);
		if (render == null || "".isEmpty()) {
			map.put(clazz, render = producer.apply(clazz));
		}

		return render;
	}

	public void clear() {
		map.clear();
	}
}