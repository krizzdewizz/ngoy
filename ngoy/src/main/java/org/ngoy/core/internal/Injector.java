package org.ngoy.core.internal;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.ngoy.core.NgoyException.wrap;
import static org.ngoy.internal.util.Util.findAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.ngoy.core.NgoyException;
import org.ngoy.core.Provider;

public class Injector {

	private final Map<Class<?>, Provider> providers;
	private final Map<Class<?>, Object> providerInstances = new HashMap<>();

	public Injector(Provider... providers) {
		Map<Class<?>, Provider> all = new LinkedHashMap<>();
		for (Provider p : providers) {
			all.put(p.getProvide(), p);
		}
		this.providers = all;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz) {
		try {
			Object object = providerInstances.get(clazz);
			if (object != null) {
				return (T) object;
			}

			Provider provider = providers.get(clazz);
			if (provider == null) {
				throw new NgoyException("no provider for '%s'", clazz.getName());
			}

			Object useValue = provider.getUseValue();
			if (useValue != null) {
				return (T) useValue;
			}

			Class<?> useClass = requireNonNull(provider.getUseClass(), "useClass must not be null");

			// System.out.println("provide: " + clazz.getName() + " -> " +
			// useClass.getName());

			Constructor<?>[] ctors = useClass.getConstructors();
			Object inst;
			if (ctors.length > 1) {
				throw new NgoyException("only 1 ctor allowed: '%s'", useClass);
			} else if (ctors.length == 0) {
				throw new NgoyException("no ctor found: '%s'", useClass);
			} else {
				Constructor<?> ctor = ctors[0];
				inst = ctor.newInstance(Stream.of(ctor.getParameterTypes())
						.map(this::get)
						.collect(toList())
						.toArray());
			}

			injectFields(useClass, inst);

			providerInstances.put(clazz, inst);

			return (T) inst;
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private void injectFields(Class<?> clazz, Object inst) throws Exception {
		try {
			for (Field f : clazz.getFields()) {
				int mods = f.getModifiers();
				if (!Modifier.isPublic(mods) || Modifier.isStatic(mods) || Modifier.isFinal(mods) || findAnnotation(f, "Inject") == null) {
					continue;
				}

				f.set(inst, get(f.getType()));
			}

		} catch (Exception e) {
			throw wrap(e);
		}
	}
}
