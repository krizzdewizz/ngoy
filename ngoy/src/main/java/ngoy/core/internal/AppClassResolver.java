package ngoy.core.internal;

import java.util.List;
import java.util.Set;

import jodd.jerry.Jerry;
import ngoy.core.Injector;

public class AppClassResolver implements Resolver {
	private final Class<?> appClass;
	private final Resolver target;

	public AppClassResolver(Class<?> appClass, Resolver target) {
		this.appClass = appClass;
		this.target = target;
	}

	public Class<?> getAppClass() {
		return appClass;
	}

	//

	public Injector getInjector() {
		return target.getInjector();
	}

	public List<CmpRef> resolveCmps(Jerry element) {
		return target.resolveCmps(element);
	}

	public Class<?> resolvePipe(String name) {
		return target.resolvePipe(name);
	}

	public List<Class<?>> resolvePipes() {
		return target.resolvePipes();
	}

	public Set<Class<?>> getCmpClasses() {
		return target.getCmpClasses();
	}
}
