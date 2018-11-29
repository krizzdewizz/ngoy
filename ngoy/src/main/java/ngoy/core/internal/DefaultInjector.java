package ngoy.core.internal;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Provider.useValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ngoy.core.Injector;
import ngoy.core.Input;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.Provider;
import ngoy.core.Util;

public class DefaultInjector implements Injector {

	@Nullable
	private static Annotation findAnnotation(Annotation[] anns, String name) {
		for (Annotation a : anns) {
			if (name.equals(a.annotationType()
					.getSimpleName())) {
				return a;
			}
		}
		return null;
	}

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
		}
		all.put(Injector.class, useValue(Injector.class, this));
		this.providers = all;
	}

	@Override
	public <T> T get(Class<T> clazz) {
		return getInternal(clazz, new HashSet<>(), false);
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
	private <T> T getInternal(Class<T> clazz, Set<Class<?>> resolving, boolean nullIfNone) {
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
					// bean injected/dev mode. find better solution
					injectFields(clazz, object, resolving, false);
					return (T) object;
				}
			}

			Provider provider = providers.get(clazz);
			if (provider == null) {
				if (nullIfNone) {
					return null;
				}
				throw new NgoyException("No provider found for %s", clazz.getName());
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
				int nParams = ctor.getParameterCount();
				Object[] arr = new Object[nParams];
				Class<?>[] paramTypes = ctor.getParameterTypes();
				Annotation[][] paramAnns = ctor.getParameterAnnotations();
				for (int i = 0; i < nParams; i++) {
					Annotation optional = findAnnotation(paramAnns[i], "Optional");
					arr[i] = getInternal(paramTypes[i], resolving, optional != null);
				}
				inst = ctor.newInstance(arr);
			}

			injectFields(useClass, inst, resolving, true);

			providerInstances.put(clazz, inst);

			return (T) inst;
		} catch (Exception e) {
			throw wrap(e);
		} finally {
			resolving.remove(clazz);
		}
	}

	public void injectFields(Class<?> clazz, Object inst, Set<Class<?>> resolving, boolean verifyInputs) {

		if (verifyInputs) {
			verifyFieldInputs(clazz, inst);
		}

		try {
			for (Field field : clazz.getFields()) {
				int mods = field.getModifiers();
				if (!Modifier.isPublic(mods) || Modifier.isStatic(mods) || Modifier.isFinal(mods) || findAnnotation(field.getAnnotations(), "Inject") == null) {
					continue;
				}

				boolean optional = findAnnotation(field.getAnnotations(), "Optional") != null;

				Object obj = getInternal(field.getType(), resolving, optional);
				if (!optional || obj != null) {
					field.set(inst, obj);
				}
			}

			for (Method meth : clazz.getMethods()) {
				int mods = meth.getModifiers();
				if (!Modifier.isPublic(mods) || Modifier.isStatic(mods) || findAnnotation(meth.getAnnotations(), "Inject") == null) {
					continue;
				}

				if (meth.getParameterCount() != 1) {
					throw new NgoyException("Inject setter method must have exactly one parameter: %s.%s", clazz.getName(), meth.getName());
				}

				boolean optional = findAnnotation(meth.getAnnotations(), "Optional") != null;

				Object obj = getInternal(meth.getParameterTypes()[0], resolving, optional);
				if (!optional || obj != null) {
					meth.invoke(inst, obj);
				}
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private void verifyFieldInputs(Class<?> cmpClazz, Object cmp) {
		try {
			for (Field field : cmpClazz.getFields()) {
				Input input = field.getAnnotation(Input.class);
				if (input == null) {
					continue;
				}
				if (!Util.isDefaultForType(field.getType(), field.get(cmp))) {
					throw new NgoyException("Input field %s.%s must not be initialized. It will be overwritten. Initialize the field in ngOnInit() instead.", cmpClazz.getName(), field.getName());
				}
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}
