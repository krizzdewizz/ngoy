package ngoy.internal.scan;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static ngoy.core.NgoyException.wrap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ngoy.core.Component;
import ngoy.core.Directive;
import ngoy.core.Injectable;
import ngoy.core.ModuleWithProviders;
import ngoy.core.Pipe;
import ngoy.core.Provider;

public class ClassScanner {

	private final Set<String> exclude = new HashSet<>();

	public ClassScanner exclude(String... names) {
		exclude.addAll(asList(names));
		return this;
	}

	public ModuleWithProviders<?> scan(String... packagePrefixes) {

		List<Class<?>> declarations = new ArrayList<>();
		List<Provider> providers = new ArrayList<>();

		jodd.io.findfile.ClassScanner scanner = new jodd.io.findfile.ClassScanner();
		scanner.registerEntryConsumer(entry -> fillIntoLists(entry, declarations, providers))
				.scanDefaultClasspath()
				.excludeAllEntries(true);

		for (String prefix : packagePrefixes) {
			scanner.includeEntries(format("%s.*", prefix));
		}

		scanner.excludeEntries(exclude.toArray(new String[exclude.size()]));

		scanner.start();

		return ModuleWithProviders.of(Void.class)
				.declarations(declarations.toArray(new Class[declarations.size()]))
				.providers(providers.toArray(new Provider[providers.size()]))
				.build();
	}

	private void fillIntoLists(jodd.io.findfile.ClassScanner.ClassPathEntry entry, List<Class<?>> declarations, List<Provider> providers) {
		try {
			Class<?> clazz = entry.loadClass();
			if (clazz.getAnnotation(Component.class) != null || clazz.getAnnotation(Directive.class) != null || clazz.getAnnotation(Pipe.class) != null) {
				declarations.add(clazz);
			} else if (clazz.getAnnotation(Injectable.class) != null) {
				providers.add(Provider.of(clazz));
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}
