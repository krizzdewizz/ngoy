package org.ngoy.core;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

public class TemplateCache {

	public static final TemplateCache DEFAULT = new TemplateCache();

	public interface CreateTemplate {
		Class<?> run(String className);
	}

	public String key(String name) {
		return format("%s.Tpl%s%s", TemplateCache.class.getPackage()
				.getName(), Math.abs(name.hashCode()), Math.abs(hashCode()));
	}

	private final Map<String, Class<?>> map = new HashMap<>();
	private boolean disabled;

	public Class<?> get(String name, CreateTemplate missing) {
		String key = key(name);
		if (disabled) {
//			System.out.println(format("template cache disabled. making class %s for path '%s'", key, name));
			return missing.run(key);
		}
		Class<?> clazz = map.get(key);
		if (clazz != null) {
			return clazz;
		}

		clazz = missing.run(key);
		map.put(key, clazz);
		return clazz;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

}
