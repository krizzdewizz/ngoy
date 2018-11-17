package ngoy.core.internal;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.util.List;
import java.util.Set;

import jodd.jerry.Jerry;
import ngoy.core.Injector;
import ngoy.core.Provider;
import ngoy.core.Util;

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
