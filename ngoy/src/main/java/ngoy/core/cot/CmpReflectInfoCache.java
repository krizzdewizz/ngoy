package ngoy.core.cot;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches a component's input info.
 * 
 * @author krizz
 */
public enum CmpReflectInfoCache {
	INSTANCE;

	private final Map<Class<?>, CmpReflectInfo> map = new HashMap<>();

	public CmpReflectInfo getInfo(Class<?> clazz) {
		CmpReflectInfo info = map.get(clazz);
		if (info == null) {
			map.put(clazz, info = CmpReflectInfo.of(clazz));
		}

		return info;
	}

	public void clear() {
		map.clear();
	}
}