package org.ngoy.core;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

public class ModuleWithProviders<T> {

	public static class Builder<T> {
		private Provider[] providers;
		private Class<?>[] declarations;
		private final Class<?> mod;

		public Builder(Class<?> mod) {
			this.mod = mod;
		}

		public Builder<T> providers(Provider... providers) {
			this.providers = providers;
			return this;
		}

		public Builder<T> declarations(Class<?>... declarations) {
			this.declarations = declarations;
			return this;
		}

		public ModuleWithProviders<T> build() {
			return new ModuleWithProviders<T>(mod, asList(declarations), asList(providers));
		}
	}

	public static <T> ModuleWithProviders<T> of(Class<?> mod, Provider... providers) {
		return new ModuleWithProviders<T>(mod, emptyList(), asList(providers));
	}

	public static <T> Builder<T> of(Class<?> mod) {
		return new Builder<T>(mod);
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
