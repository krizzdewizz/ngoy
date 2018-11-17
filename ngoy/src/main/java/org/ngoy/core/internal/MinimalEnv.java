package org.ngoy.core.internal;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.util.List;
import java.util.Set;

import org.ngoy.core.Injector;
import org.ngoy.core.Provider;
import org.ngoy.core.Util;

import jodd.jerry.Jerry;

public class MinimalEnv {
	private static final List<CmpRef> CONTAINER = asList(new CmpRef(ContainerComponent.class, Util.getTemplate(ContainerComponent.class), false));

	public static final Injector INJECTOR = new DefaultInjector(Provider.of(ContainerComponent.class));

	public static final Resolver RESOLVER = new Resolver() {

		@Override
		public Injector getInjector() {
			return INJECTOR;
		}

		@Override
		public List<CmpRef> resolveCmps(Jerry element) {
			return element.is(ContainerComponent.SELECTOR) ? CONTAINER : emptyList();
		}

		@Override
		public Class<?> resolvePipe(String name) {
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
}
