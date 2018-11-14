package org.ngoy.core.internal;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.ngoy.core.Injector;
import org.ngoy.core.Provider;
import org.ngoy.core.Util;

public class MinimalEnv {
	private static final List<CmpRef> CONTAINER = asList(new CmpRef(ContainerComponent.class, Util.getTemplate(ContainerComponent.class), false));

	public static final Injector INJECTOR = new DefaultInjector(Provider.of(ContainerComponent.class));

	public static final Resolver RESOLVER = new Resolver() {

		@Override
		public Injector getInjector() {
			return INJECTOR;
		}

		@Override
		public List<CmpRef> resolveCmps(Element element) {
			return element.is(ContainerComponent.SELECTOR) ? CONTAINER : null;
		}

		@Override
		public Class<?> resolvePipe(String name) {
			return null;
		}

		@Override
		public String resolveCmpClass(String cmpClass) {
			return null;
		}

		@Override
		public Set<Class<?>> getCmpClasses() {
			return emptySet();
		}
	};
}
