package ngoy.core;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

/**
 * Once a template first used, it is compiled to byte code and stored in the
 * cache for later retrieval when the template is run again.
 * 
 * @author krizz
 */
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

	/**
	 * @return Whether the cache is disabled.
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * While developing, disable the cache to get changes to the template and/or
	 * model upon every rendering.
	 * 
	 * @param disabled
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
