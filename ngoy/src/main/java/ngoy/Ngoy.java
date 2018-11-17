package ngoy;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Provider.of;
import static ngoy.core.Provider.useClass;
import static ngoy.core.Provider.useValue;
import static ngoy.core.Util.copyToString;
import static ngoy.core.Util.getTemplate;
import static ngoy.core.Util.isSet;
import static ngoy.core.Util.newPrintStream;
import static ngoy.internal.parser.visitor.XDom.matchesAttributeBinding;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import jodd.jerry.Jerry;
import ngoy.common.PipesModule;
import ngoy.core.Component;
import ngoy.core.Context;
import ngoy.core.Directive;
import ngoy.core.Events;
import ngoy.core.Injector;
import ngoy.core.LocaleProvider;
import ngoy.core.ModuleWithProviders;
import ngoy.core.NgModule;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;
import ngoy.core.Pipe;
import ngoy.core.Provide;
import ngoy.core.Provider;
import ngoy.core.Renderer;
import ngoy.core.TemplateCache;
import ngoy.core.internal.CmpRef;
import ngoy.core.internal.CoreInternalModule;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.DefaultInjector;
import ngoy.core.internal.MinimalEnv;
import ngoy.core.internal.Resolver;
import ngoy.internal.parser.ByteCodeTemplate;
import ngoy.internal.parser.Parser;
import ngoy.translate.TranslateModule;
import ngoy.translate.TranslateService;

public class Ngoy implements Renderer {
	public static class Builder {
		private final Class<?> appRoot;
		private final Config config = new Config();
		private Provider[] providers;
		private Injector[] injectors;
		private TemplateCache cache;
		private ModuleWithProviders<?>[] modules;

		public Builder(Class<?> appRoot) {
			this.appRoot = appRoot;
		}

		public Builder providers(Provider... providers) {
			this.providers = providers;
			return this;
		}

		public Builder injectors(Injector... injectors) {
			this.injectors = injectors;
			return this;
		}

		public Builder modules(ModuleWithProviders<?>... modules) {
			this.modules = modules;
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
			return new Ngoy(appRoot, //
					config, //
					cache, //
					injectors != null ? injectors : new Injector[0], //
					modules != null ? modules : new ModuleWithProviders[0], //
					providers != null ? providers : new Provider[0]);
		}
	}

	public static Builder app(Class<?> appRoot) {
		return new Builder(appRoot);
	}

	/**
	 * Renders the given string.
	 */
	public static void renderString(String template, Context context, OutputStream out, Config... config) {
		if (context == null) {
			context = Context.of();
		}
		new Ngoy(optionalConfig(config)).renderTemplate(template, false, (Ctx) context.internal(), out);
	}

	/**
	 * Renders the given template
	 * 
	 * @param templatePath see {@link Class#getResourceAsStream(String)}
	 */
	public static void render(String templatePath, Context context, OutputStream out, Config... config) {
		if (context == null) {
			context = Context.of();
		}
		new Ngoy(optionalConfig(config)).renderTemplate(templatePath, true, (Ctx) context.internal(), out);
	}

	private void renderTemplate(String templateOrPath, boolean templateIsPath, Ctx ctx, OutputStream out) {
		String tpl;
		if (templateIsPath) {
			InputStream in = getClass().getResourceAsStream(templateOrPath);
			if (in == null) {
				throw new NgoyException("Template could not be found: '%s'", templateOrPath);
			}
			try (InputStream inn = in) {
				tpl = copyToString(inn);
			} catch (Exception e) {
				throw wrap(e);
			}
		} else {
			tpl = templateOrPath;
		}

		Class<?> clazz = createTemplate(cache.key(templateOrPath), createParser(null, MinimalEnv.RESOLVER, config), tpl, config.contentType);
		invokeRender(clazz, ctx, newPrintStream(out));
	}

	private static Config optionalConfig(Config... config) {
		return config.length > 0 ? config[0] : new Config();
	}

	public static class Config {
		public Locale locale;
		public String translateBundle;
		public boolean inlineComponents;
		public String contentType;
	}

	private final Config config;
	private final Class<?> appRoot;
	private Object appInstance;
	private Resolver resolver;
	private Injector injector;
	private final TemplateCache cache;
	private final Events events = new Events();

	protected Ngoy(Config config) {
		this(Object.class, config, null, new Injector[0], new ModuleWithProviders[0]);
	}

	protected Ngoy(Class<?> appRoot, Config config, TemplateCache cache, Injector[] injectors, ModuleWithProviders<?>[] modules, Provider... rootProviders) {
		this.appRoot = appRoot;
		this.config = config;
		this.cache = cache != null ? cache : TemplateCache.DEFAULT;
		this.init(injectors, modules, rootProviders);
	}

	private void init(Injector[] injectors, ModuleWithProviders<?>[] modules, Provider... rootProviders) {

		List<Provider> cmpProviders = new ArrayList<>();
		Map<String, Provider> cmpDecls = new LinkedHashMap<>(); // order of css
		Map<String, Provider> pipeDecls = new HashMap<>();

		if (provides(LocaleProvider.class, rootProviders) == null) {
			cmpProviders.add(useValue(LocaleProvider.class, new LocaleProvider.Default(config.locale != null ? config.locale : Locale.getDefault())));
		}

		String translateBundle = config.translateBundle;
		boolean hasTranslate = isSet(translateBundle);
		if (hasTranslate) {
			addModuleDecls(TranslateModule.class, cmpDecls, pipeDecls, cmpProviders);
		}

		addModuleDecls(CoreInternalModule.class, cmpDecls, pipeDecls, cmpProviders);
		addModuleDecls(PipesModule.class, cmpDecls, pipeDecls, cmpProviders);
		addModuleDecls(appRoot, cmpDecls, pipeDecls, cmpProviders);

		List<Provider> all = new ArrayList<>();

		for (ModuleWithProviders<?> mod : modules) {
			addModuleDecls(mod.getModule(), cmpDecls, pipeDecls, cmpProviders);
			addDecls(toProviders(mod.getDeclarations()), cmpDecls, pipeDecls);
			for (Provider p : mod.getProviders()) {
				all.add(p);
			}
		}

		// collection done

		resolver = createResolver(cmpDecls, pipeDecls);

		all.add(useValue(Resolver.class, resolver));
		all.add(useValue(Renderer.class, this));
		all.add(useValue(Events.class, events));
		all.addAll(cmpProviders);
		all.addAll(cmpDecls.values());
		all.addAll(pipeDecls.values());
		all.addAll(asList(rootProviders));

		Provider appRootProvider = provides(appRoot, rootProviders);

		DefaultInjector inj = new DefaultInjector(injectors, all.toArray(new Provider[all.size()]));
		injector = inj;

		if (appRootProvider != null && appRootProvider.getUseValue() != null) {
			appInstance = appRootProvider.getUseValue();
			inj.injectFields(appRoot, appInstance, new HashSet<>());
		} else {
			inj.put(of(appRoot));
			appInstance = injector.get(appRoot);
		}

		if (hasTranslate) {
			injector.get(TranslateService.class)
					.setBundle(translateBundle);
		}
	}

	private List<Provider> toProviders(List<Class<?>> list) {
		return list.stream()
				.map(Provider::of)
				.collect(toList());
	}

	private Resolver createResolver(Map<String, Provider> cmpDecls, Map<String, Provider> pipeDecls) {
		return new Resolver() {
			@Override
			public List<CmpRef> resolveCmps(Jerry node) {
				return cmpDecls.entrySet()
						.stream()
						.filter(entry -> {
							String key = entry.getKey();
							if (node.is(key)) {
								return true;
							}

							if (key.startsWith("[")) {
								return matchesAttributeBinding(node, key);
							}

							return false;
						})
						.map(Map.Entry::getValue)
						.map(Provider::getProvide)
						.map(clazz -> {
							boolean directive = clazz.getAnnotation(Directive.class) != null;
							return new CmpRef(clazz, directive ? null : getTemplate(clazz), directive);
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

			@Override
			public Class<?> resolveCmpClass(Class<?> cmpClass) {
				return cmpClass != null ? cmpClass : appRoot;
			}

			@Override
			public Set<Class<?>> getCmpClasses() {
				Set<Class<?>> all = new LinkedHashSet<>();
				all.add(appRoot);
				cmpDecls.values()
						.stream()
						.map(Provider::getProvide)
						.forEach(all::add);
				return all;
			}
		};
	}

	private Provider provides(Class<?> theClass, Provider... rootProviders) {
		return Stream.of(rootProviders)
				.filter(p -> p.getProvide()
						.equals(theClass))
				.findFirst()
				.orElse(null);
	}

	@Override
	public void render(OutputStream out) {
		try {
			if (appInstance instanceof OnInit) {
				((OnInit) appInstance).ngOnInit();
			}

			Ctx ctx = Ctx.of(appInstance, injector);

			events.tick();

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
			addDecls(toProviders(asList(mod.declarations())), targetCmps, targetPipes);

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

	private void addDecls(List<Provider> providers, Map<String, Provider> targetCmps, Map<String, Provider> targetPipes) {
		for (Provider p : providers) {
			Pipe pipe = p.getProvide()
					.getAnnotation(Pipe.class);
			if (pipe != null) {
				targetPipes.put(pipe.value(), p);
			}

			Component cmp = p.getProvide()
					.getAnnotation(Component.class);
			if (cmp != null) {
				Provider existing = targetCmps.put(cmp.selector(), p);
				if (existing != null) {
					throw new NgoyException(
							"More than one component matched on the selector '%s'. Make sure that only one component's selector can match a given element. Conflicting components: %s, %s",
							cmp.selector(), existing.getProvide()
									.getName(),
							p.getProvide()
									.getName());
				}
			}

			Directive dir = p.getProvide()
					.getAnnotation(Directive.class);
			if (dir != null) {
				targetCmps.put(dir.selector(), p);
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
			Component cmp = appRoot.getAnnotation(Component.class);
			if (cmp != null) {
				contentType = cmp.contentType();
			}
		}
		return contentType;
	}

	private static Parser createParser(@Nullable Class<?> appRoot, @Nullable Resolver r, Config config) {
		Parser parser = new Parser(r);
		parser.inlineComponents = config.inlineComponents;

		parser.contentType = getContentType(appRoot, config);
		return parser;
	}

	public <T> Ngoy publish(Object event, T payload) {
		events.publish(event, payload);
		return this;
	}

	public void destroy() {
		appInstance = null;
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("usage: Ngoy <expr>");
			return;
		}
		Context context = Context.of("args", args)
				.variable("sys", System.getProperties());
		String expr = args[0];
		renderString(expr, context, System.out);
	}
}
