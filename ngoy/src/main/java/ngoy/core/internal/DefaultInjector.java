package ngoy.core.internal;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Provider.useValue;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ngoy.core.Injector;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.Provider;

public class DefaultInjector implements Injector {

	private interface Factory {
		Object create();
	}

	private interface Injection {
		void apply(Object instance) throws Throwable;
	}

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
	private final Set<Class<?>> cmpDecls;
	private final Map<Class<?>, Factory> factories = new HashMap<>();
	private final Map<Object, Object> selectorToCmpDecls;

	public DefaultInjector(Set<Class<?>> cmpDecls, Map<Object, Object> selectorToCmpDecls, Injector[] more, Provider... providers) {
		this.cmpDecls = cmpDecls;
		this.selectorToCmpDecls = selectorToCmpDecls;
		this.moreInjectors = more;
		Map<Class<?>, Provider> all = new LinkedHashMap<>();
		for (Provider p : providers) {
			all.put(p.getProvide(), p);
		}
		all.put(Injector.class, useValue(Injector.class, this));
		this.providers = all;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getNewCmp(String selector) {
		Class<?> decl = (Class<?>) selectorToCmpDecls.get(selector);
		return decl == null ? null : (T) getNew(decl);
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

			Object object = providerInstances.get(clazz);
			if (object != null) {
				return (T) object;
			}

			Factory factory = factories.get(clazz);
			if (factory != null) {
				return (T) factory.create();
			}

			if (!cmpDecls.contains(clazz) && !Injector.class.isAssignableFrom(clazz)) {
				for (Injector inj : moreInjectors) {
					if ((object = inj.get(clazz)) != null) {
						providerInstances.put(clazz, object);
						applyInjections(object, fieldInjections(clazz, resolving));
						return (T) object;
					}
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

			//

			if (resolving.contains(clazz)) {
				throw new NgoyException("Dependency cycle detected. Classes involved: %s", resolving.stream()
						.map(Class::getName)
						.collect(joining(", ")));
			}

			resolving.add(clazz);

			Class<?> useClass = requireNonNull(provider.getUseClass(), "useClass must not be null");

			Constructor<?>[] ctors = useClass.getConstructors();
			if (ctors.length > 1) {
				throw new NgoyException("Only one constructor allowed: %s", useClass.getName());
			} else if (ctors.length == 0) {
				throw new NgoyException("No constructor found: %s", useClass.getName());
			}

			Constructor<?> ctor = ctors[0];
			int nParams = ctor.getParameterCount();
			Object[] arr = new Object[nParams];
			Class<?>[] paramTypes = ctor.getParameterTypes();
			if (nParams > 0) {
				Annotation[][] paramAnns = ctor.getParameterAnnotations();
				for (int i = 0; i < nParams; i++) {
					Annotation optional = findAnnotation(paramAnns[i], "Optional");
					arr[i] = getInternal(paramTypes[i], resolving, optional != null);
				}
			}

			List<Injection> fieldInjections = fieldInjections(useClass, resolving);

			MethodHandle constructor = MethodHandles.lookup()
					.unreflectConstructor(ctor);

			factory = () -> {
				try {
					Object instance = constructor.invokeWithArguments(arr);
					applyInjections(instance, fieldInjections);
					providerInstances.put(clazz, instance);
					return instance;
				} catch (Throwable e) {
					throw wrap(e);
				}
			};

			factories.put(clazz, factory);

			return (T) factory.create();

		} catch (Exception e) {
			throw wrap(e);
		} finally {
			resolving.remove(clazz);
		}
	}

	@Override
	public <T> T getNew(Class<T> clazz) {
		providerInstances.remove(clazz);
		return get(clazz);
	}

	public void applyInjections(Object instance, List<Injection> injections) {
		try {
			for (Injection f : injections) {
				f.apply(instance);
			}
		} catch (Throwable e) {
			throw wrap(e);
		}
	}

	public List<Injection> fieldInjections(Class<?> clazz, Set<Class<?>> resolving) {
		try {
			Lookup lookup = MethodHandles.lookup();
			final List<Injection> injections = new ArrayList<>();
			for (Field field : clazz.getFields()) {

				if (findAnnotation(field.getAnnotations(), "Inject") == null) {
					continue;
				}

				int mods = field.getModifiers();
				if (Modifier.isStatic(mods) || Modifier.isFinal(mods)) {
					throw new NgoyException("@Inject annotated field must be public, non-final, non-static: %s.%s", clazz.getName(), field.getName());
				}

				boolean optional = findAnnotation(field.getAnnotations(), "Optional") != null;

				Object obj = getInternal(field.getType(), resolving, optional);
				if (!optional || obj != null) {
					MethodHandle setter = lookup.unreflectSetter(field);
					injections.add(i -> setter.invoke(i, obj));
				}
			}

			for (Method meth : clazz.getMethods()) {
				if (findAnnotation(meth.getAnnotations(), "Inject") == null) {
					continue;
				}

				int mods = meth.getModifiers();
				if (Modifier.isStatic(mods)) {
					throw new NgoyException("@Inject annotated method must be public, non-static: %s.%s", clazz.getName(), meth.getName());
				}

				if (meth.getParameterCount() != 1) {
					throw new NgoyException("@Inject annotated method must have exactly one parameter: %s.%s", clazz.getName(), meth.getName());
				}

				boolean optional = findAnnotation(meth.getAnnotations(), "Optional") != null;

				Object obj = getInternal(meth.getParameterTypes()[0], resolving, optional);
				if (!optional || obj != null) {
					MethodHandle methHandle = lookup.unreflect(meth);
					injections.add(i -> methHandle.invoke(i, obj));
				}
			}

			return injections;
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}
