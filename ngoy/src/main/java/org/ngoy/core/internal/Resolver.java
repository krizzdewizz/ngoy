package org.ngoy.core.internal;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.util.List;
import java.util.Set;

import org.ngoy.core.Injector;

import jodd.jerry.Jerry;

public interface Resolver {
	Resolver DEFAULT = new Resolver() {

		@Override
		public List<CmpRef> resolveCmps(Jerry element) {
			return emptyList();
		}

		@Override
		public Class<?> resolvePipe(String name) {
			return null;
		}

		@Override
		public Injector getInjector() {
			return null;
		}

		@Override
		public Class<?> resolveCmpClass(Class<?> cmpClass) {
			return null;
		}

		@Override
		public Set<Class<?>> getCmpClasses() {
			return emptySet();
		}
	};

	Injector getInjector();

	List<CmpRef> resolveCmps(Jerry element);

	Class<?> resolvePipe(String name);

	Class<?> resolveCmpClass(Class<?> cmpClass);

	Set<Class<?>> getCmpClasses();
}