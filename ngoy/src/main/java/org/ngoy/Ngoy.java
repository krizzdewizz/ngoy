package org.ngoy;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.ngoy.core.NgoyException.wrap;
import static org.ngoy.core.Provider.of;
import static org.ngoy.core.Provider.useClass;
import static org.ngoy.core.Provider.useValue;
import static org.ngoy.internal.util.Util.copyToString;
import static org.ngoy.internal.util.Util.isSet;
import static org.ngoy.internal.util.Util.newPrintStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.ngoy.common.TranslateModule;
import org.ngoy.common.TranslateService;
import org.ngoy.core.Component;
import org.ngoy.core.Directive;
import org.ngoy.core.ElementRef;
import org.ngoy.core.NgModule;
import org.ngoy.core.NgoyException;
import org.ngoy.core.OnDestroy;
import org.ngoy.core.OnInit;
import org.ngoy.core.Pipe;
import org.ngoy.core.Provide;
import org.ngoy.core.Provider;
import org.ngoy.core.TemplateCache;
import org.ngoy.core.internal.CmpRef;
import org.ngoy.core.internal.ContainerComponent;
import org.ngoy.core.internal.Ctx;
import org.ngoy.core.internal.Injector;
import org.ngoy.core.internal.Resolver;
import org.ngoy.internal.parser.ByteCodeTemplate;
import org.ngoy.internal.parser.Parser;
import org.ngoy.internal.util.Nullable;

public class Ngoy {
	public static class Builder {
		private Class<?> appRoot;
		private Provider[] providers;
		private Config config = new Config();
		private TemplateCache cache;

		public Builder(Class<?> appRoot) {
			this.appRoot = appRoot;
		}

		public Builder providers(Provider... providers) {
			this.providers = providers;
			return this;
		}

		public Builder parseBody(boolean parseBody) {
			config.parseBody = parseBody;
			return this;
		}

		public Builder inlineComponents(boolean inlineComponents) {
			config.inlineComponents = inlineComponents;
			return this;
		}

		public Builder contentType(String contentType) {
			config.contentType = contentType;
			return this;
		}

		public Builder cache(TemplateCache cache) {
			this.cache = cache;
			return this;
		}

		public Builder locale(Locale locale) {
			this.config.locale = locale;
			return this;
		}

		public Builder translateBundle(String translateBundle) {
			this.config.translateBundle = translateBundle;
			return this;
		}

		public Ngoy build() {
			return new Ngoy(appRoot, config, cache, providers != null ? providers : new Provider[0]);
		}
	}

	public static Builder app(Class<?> appRoot) {
		return new Builder(appRoot);
	}

	public static void renderString(String string, Ctx ctx, OutputStream out, Config... config) {
		new Ngoy(optionalConfig(config)).renderTemplate(string, false, ctx, out);
	}

	public static void render(String templatePath, Ctx ctx, OutputStream out, Config... config) {
		new Ngoy(optionalConfig(config)).renderTemplate(templatePath, true, ctx, out);
	}

	public static String getTemplate(Class<?> clazz) {
		try {
			Component cmp = clazz.getAnnotation(Component.class);
			String templateUrl = cmp.templateUrl();
			String tpl;
			if (isSet(templateUrl)) {
				InputStream in = clazz.getResourceAsStream(templateUrl);
				if (in == null) {
					throw new NgoyException("template could not be found: '%s'", templateUrl);
				}
				tpl = copyToString(in);
			} else {
				tpl = cmp.template();
			}
			return tpl;
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private void renderTemplate(String templateOrPath, boolean templateIsPath, Ctx ctx, OutputStream out) {
		String tpl = templateIsPath ? copyToString(getClass().getResourceAsStream(templateOrPath)) : templateOrPath;
		Class<?> clazz = createTemplate(cache.key(templateOrPath), createParser(null, null, config), tpl, config.contentType);
		invokeRender(clazz, ctx, newPrintStream(out));
	}

	private static Config optionalConfig(Config... config) {
		return config.length > 0 ? config[0] : new Config();
	}

	public static class Config {
		public Locale locale;
		public boolean parseBody;
		public String translateBundle;
		public boolean inlineComponents;
		public String contentType;
	}

	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	private final Config config;
	private final Class<?> appRoot;
	private Object appInstance;
	private Resolver resolver;
	private Injector injector;
	private TemplateCache cache;

	protected Ngoy(Config config) {
		this(Object.class, config, null);
	}

	protected Ngoy(Class<?> appRoot, Config config, TemplateCache cache, Provider... rootProviders) {
		this.appRoot = appRoot;
		this.config = config;
		this.cache = cache != null ? cache : TemplateCache.DEFAULT;
		this.init(rootProviders);
	}

	private void init(Provider... rootProviders) {

		List<Provider> cmpProviders = new ArrayList<>();
		Map<String, Provider> cmpDecls = new HashMap<>();
		Map<String, Provider> pipeDecls = new HashMap<>();

		boolean providesLocale = Stream.of(rootProviders)
				.filter(p -> p.getProvide()
						.equals(Locale.class))
				.findFirst()
				.isPresent();
		if (!providesLocale) {
			cmpProviders.add(useValue(Locale.class, config.locale != null ? config.locale : DEFAULT_LOCALE));
		}

		String translateBundle = config.translateBundle;
		boolean hasTranslate = isSet(translateBundle);
		if (hasTranslate) {
			addModuleDecls(TranslateModule.class, cmpDecls, pipeDecls, cmpProviders);
		}

		cmpDecls.put(ContainerComponent.SELECTOR, of(ContainerComponent.class));

		addModuleDecls(appRoot, cmpDecls, pipeDecls, cmpProviders);

		List<Provider> all = new ArrayList<>();
		all.add(of(appRoot));
		all.addAll(asList(rootProviders));
		all.addAll(cmpProviders);
		all.addAll(cmpDecls.values());
		all.addAll(pipeDecls.values());

		injector = new Injector(all.toArray(new Provider[all.size()]));

		if (hasTranslate) {
			injector.get(TranslateService.class)
					.setBundle(translateBundle);
		}

		resolver = new Resolver() {
			@Override
			public List<CmpRef> resolveCmps(ElementRef node) {
				return cmpDecls.entrySet()
						.stream()
						.filter(entry -> {
							String key = entry.getKey();
							if (node.is(key)) {
								return true;
							}

							if (key.startsWith("[")) {
								return node.is(format("[%s]", key)); // directive name same as @Input
							}

							return false;
						})
						.map(Map.Entry::getValue)
						.map(Provider::getProvide)
						.map(clazz -> {
							boolean directive = clazz.getAnnotation(Directive.class) != null;
							Component cmp = clazz.getAnnotation(Component.class);
							return new CmpRef(clazz, directive ? null : getTemplate(clazz), directive, cmp == null ? "" : cmp.contentType());
						})
						.collect(toList());
			}

			@Override
			public Class<?> resolvePipe(String name) {
				Provider provider = pipeDecls.get(name);
				return provider != null ? provider.getProvide() : null;
			}

			@Override
			public Injector getInjector() {
				return injector;
			}
		};
	}

	public void render(OutputStream out) {
		try {
			if (appInstance == null) {
				appInstance = injector.get(appRoot);
			}

			if (appInstance instanceof OnInit) {
				((OnInit) appInstance).ngOnInit();
			}

			Ctx ctx = Ctx.of(appInstance, injector);

			parseAndRender(appRoot, createParser(appRoot, resolver, config), ctx, newPrintStream(out));

			if (appInstance instanceof OnDestroy) {
				((OnDestroy) appInstance).ngOnDestroy();
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addModuleDecls(Class<?> clazz, Map<String, Provider> targetCmps, Map<String, Provider> targetPipes, List<Provider> providers) {
		Component cmp = clazz.getAnnotation(Component.class);
		if (cmp != null) {
			addDecls(asList(cmp.declarations()).stream()
					.map(Provider::of)
					.collect(toList()), targetCmps, targetPipes);

			for (Class<?> prov : cmp.providers()) {
				providers.add(of(prov));
			}

			for (Provide prov : cmp.provide()) {
				Class p = prov.provide();
				Class c = prov.useClass();
				providers.add(useClass(p, (Class<?>) c));
			}
		}

		NgModule mod = clazz.getAnnotation(NgModule.class);
		if (mod != null) {
			addDecls(asList(mod.declarations()).stream()
					.map(Provider::of)
					.collect(toList()), targetCmps, targetPipes);

			for (Class<?> imp : mod.imports()) {
				addModuleDecls(imp, targetCmps, targetPipes, providers);
			}

			for (Class<?> prov : mod.providers()) {
				providers.add(of(prov));
			}
			for (Provide prov : mod.provide()) {
				Class p = prov.provide();
				Class c = prov.useClass();
				providers.add(useClass(p, c));
			}
		}
	}

	private void addDecls(List<Provider> declarations, Map<String, Provider> targetCmps, Map<String, Provider> targetPipes) {
		for (Provider decl : declarations) {
			Pipe pipe = decl.getProvide()
					.getAnnotation(Pipe.class);
			if (pipe != null) {
				targetPipes.put(pipe.value(), decl);
			}

			Component cmp = decl.getProvide()
					.getAnnotation(Component.class);
			if (cmp != null) {
				targetCmps.put(cmp.selector(), decl);
			}

			Directive dir = decl.getProvide()
					.getAnnotation(Directive.class);
			if (dir != null) {
				targetCmps.put(dir.selector(), decl);
			}
		}
	}

	protected void parseAndRender(Class<?> appRoot, Parser parser, Ctx ctx, PrintStream out) {
		invokeRender(cache.get(appRoot.getName(), className -> createTemplate(className, parser, getTemplate(appRoot), getContentType(appRoot, config))), ctx, out);
	}

	private void invokeRender(Class<?> templateClass, Ctx ctx, PrintStream out) {
		try {
			ctx.setOut(out, getContentType(appRoot, config));
			Method m = templateClass.getMethod("render", Ctx.class);
			m.invoke(null, ctx);
		} catch (Exception e) {
			throw wrap(e);
		} finally {
			ctx.resetOut();
		}
	}

	private Class<?> createTemplate(String className, Parser parser, String template, String contentType) {
		ByteCodeTemplate bct = new ByteCodeTemplate(className, contentType);
		parser.parse(template, bct);
		return bct.getClassFile()
				.defineClass();
	}

	private static String getContentType(@Nullable Class<?> appRoot, Config config) {
		String contentType = config.contentType;
		if ((contentType == null || contentType.isEmpty()) && appRoot != null) {
			contentType = appRoot.getAnnotation(Component.class)
					.contentType();
		}
		return contentType;
	}

	private static Parser createParser(@Nullable Class<?> appRoot, @Nullable Resolver r, Config config) {
		Parser parser = new Parser(r);
		parser.parseBody = config.parseBody;
		parser.inlineComponents = config.inlineComponents;

		parser.contentType = getContentType(appRoot, config);
		return parser;
	}
}
