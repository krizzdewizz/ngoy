package ngoy;

import java.util.function.Function;

import ngoy.Ngoy;
import ngoy.core.ModuleWithProviders;

public class Modules<T> implements Function<Ngoy.Builder<?>, Ngoy.Builder<?>> {

	@SafeVarargs
	public static <T> Modules<T> of(ModuleWithProviders<T>... mods) {
		return new Modules<T>(mods);
	}

	private ModuleWithProviders<T>[] mods;

	@SafeVarargs
	private Modules(ModuleWithProviders<T>... mods) {
		this.mods = mods;
	}

	@Override
	public Ngoy.Builder<?> apply(Ngoy.Builder<?> t) {
		return t.modules(mods);
	}
}