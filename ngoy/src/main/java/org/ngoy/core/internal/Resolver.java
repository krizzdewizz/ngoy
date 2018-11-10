package org.ngoy.core.internal;

import static java.util.Collections.emptyList;

import java.util.List;

import org.ngoy.core.ElementRef;
import org.ngoy.core.Injector;

public interface Resolver {
	Resolver DEFAULT = new Resolver() {

		@Override
		public List<CmpRef> resolveCmps(ElementRef element) {
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
		public String resolveCmpClass(String cmpClass) {
			return null;
		}
	};

	Injector getInjector();

	List<CmpRef> resolveCmps(ElementRef element);

	Class<?> resolvePipe(String name);

	String resolveCmpClass(String cmpClass);
}