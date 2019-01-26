package ngoy.hyperml.base;

import static ngoy.internal.parser.Inputs.fieldInputs;

import java.util.HashMap;
import java.util.Map;

import ngoy.internal.parser.Inputs.ReflectInput;

/**
 * Caches a component's input info.
 * 
 * @author krizz
 */
public enum InputsCache {
	INSTANCE;

	private final Map<Class<?>, Map<String, ReflectInput>> map = new HashMap<>();

	public Map<String, ReflectInput> getInputs(Class<?> clazz) {
		Map<String, ReflectInput> inputs = map.get(clazz);
		if (inputs == null) {
			map.put(clazz, inputs = fieldInputs(clazz));
		}

		return inputs;
	}

	public void clear() {
		map.clear();
	}
}