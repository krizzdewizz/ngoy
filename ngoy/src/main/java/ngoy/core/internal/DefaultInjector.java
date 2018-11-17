package ngoy.core.internal;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Provider.useValue;
import static ngoy.core.Util.findAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import ngoy.core.Injector;
import ngoy.core.NgoyException;
import ngoy.core.Provider;

public class DefaultInjector implements Injector {

	private final Map<Class<?>, Provider> providers;
	private final Map<Class<?>, Object> providerInstances = new HashMap<>();
	private final Injector[] moreInjectors;

	public DefaultInjector(Provider... providers) {
		this(new Injector[0], providers);
	}

	public DefaultInjector(Injector[] more, Provider... providers) {
		this.moreInjectors = more;
		Map<Class<?>, Provider> all = new LinkedHashMap<>();
		for (Provider p : providers) {
			all.put(p.getProvide(), p);
//			Provider existing = all.put(p.getProvide(), p);
//			if (existing != null) {
//				throw new NgoyException("More than one provider for %s: %s, %s", p.getProvide()
//						.getName(), existing, p);
//			}
		}
		all.put(Injector.class, useValue(Injector.class, this));
		this.providers = all;
	}

	@Override
	public <T> T get(Class<T> clazz) {
		return getInternal(clazz, new HashSet<>());
	}

	public void put(Provider provider) {
		Class<?> provide = provider.getProvide();
		Object useValue = provider.getUseValue();

		providers.put(provide, provider);
		if (useValue != null) {
			providerInstances.put(provide, useValue);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getInternal(Class<T> clazz, Set<Class<?>> resolving) {
		try {

			if (resolving.contains(clazz)) {
				throw new NgoyException("Dependency cycle detected. Classes involved: %s", resolving.stream()
						.map(Class::getName)
						.collect(joining(", ")));
			}

			resolving.add(clazz);

			Object object = providerInstances.get(clazz);
			if (object != null) {
				return (T) object;
			}

			for (Injector inj : moreInjectors) {
				if ((object = inj.get(clazz)) != null) {
					providerInstances.put(clazz, object);
					injectFields(clazz, object, resolving);
					return (T) object;
				}
			}

			Provider provider = providers.get(clazz);
			if (provider == null) {
				throw new NgoyException("No provider for %s", clazz.getName());
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
				throw new NgoyException("Only one constructor allowed: %s", useClass.getName());
			} else if (ctors.length == 0) {
				throw new NgoyException("No constructor found: %s", useClass.getName());
			} else {
				Constructor<?> ctor = ctors[0];
				inst = ctor.newInstance(Stream.of(ctor.getParameterTypes())
						.map(pt -> getInternal(pt, resolving))
						.collect(toList())
						.toArray());
			}

			injectFields(useClass, inst, resolving);

			providerInstances.put(clazz, inst);

			return (T) inst;
		} catch (Exception e) {
			throw wrap(e);
		} finally {
			resolving.remove(clazz);
		}
	}

	public void injectFields(Class<?> clazz, Object inst, Set<Class<?>> resolving) {
		try {
			for (Field field : clazz.getFields()) {
				int mods = field.getModifiers();
				if (!Modifier.isPublic(mods) || Modifier.isStatic(mods) || Modifier.isFinal(mods) || findAnnotation(field, "Inject") == null) {
					continue;
				}

				field.set(inst, getInternal(field.getType(), resolving));
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}