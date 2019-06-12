package ngoy.core.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.synchronizedMap;

/**
 * Caches a compiled component renderer.
 *
 * @author krizz
 */
public enum TemplateRenderCache {
    INSTANCE;

    private final Map<Class<?>, TemplateRender> map = synchronizedMap(new HashMap<>());

    public TemplateRender compile(Class<?> clazz, TemplateCompiler compiler) {
        return getTemplateRender(clazz, compiler::compile);
    }

    public TemplateRender getTemplateRender(Class<?> clazz, Function<Class<?>, TemplateRender> producer) {
        TemplateRender render = map.get(clazz);
        if (render == null) {
            map.put(clazz, render = producer.apply(clazz));
        }

        return render;
    }

    public void clear() {
        map.clear();
    }
}