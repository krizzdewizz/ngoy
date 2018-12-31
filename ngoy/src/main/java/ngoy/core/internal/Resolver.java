package ngoy.core.internal;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.util.List;
import java.util.Set;

import jodd.jerry.Jerry;
import ngoy.core.Injector;

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
		public Set<Class<?>> getCmpClasses() {
			return emptySet();
		}

		@Override
		public Class<?> getAppClass() {
			return null;
		}

		@Override
		public List<Class<?>> resolvePipes() {
			return emptyList();
		}
	};

	Injector getInjector();

	List<CmpRef> resolveCmps(Jerry element);

	Class<?> resolvePipe(String name);

	List<Class<?>> resolvePipes();

	Set<Class<?>> getCmpClasses();

	Class<?> getAppClass();
}