package org.ngoy.core;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

public class ModuleWithProviders<T> {

	public static <T> ModuleWithProviders<T> of(Class<?> mod, Provider... providers) {
		return of(mod, emptyList(), providers);
	}

	public static <T> ModuleWithProviders<T> of(Class<?> mod, List<Class<?>> declarations, Provider... providers) {
		return new ModuleWithProviders<>(mod, declarations, asList(providers));
	}

	private final Class<?> mod;
	private final List<Provider> providers;
	private final List<Class<?>> declarations;

	private ModuleWithProviders(Class<?> mod, List<Class<?>> declarations, List<Provider> providers) {
		this.mod = mod;
		this.declarations = declarations;
		this.providers = providers;
	}

	public Class<?> getModule() {
		return mod;
	}

	public List<Provider> getProviders() {
		return providers;
	}

	public List<Class<?>> getDeclarations() {
		return declarations;
	}
}
