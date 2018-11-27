package ngoy;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Provider.of;
import static ngoy.core.Provider.useClass;
import static ngoy.core.Provider.useValue;
import static ngoy.core.Util.copyToString;
import static ngoy.core.Util.getTemplate;
import static ngoy.core.Util.isSet;
import static ngoy.core.Util.newPrintStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.stream.Stream;

import jodd.jerry.Jerry;
import ngoy.common.CommonModule;
import ngoy.common.DatePipe;
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
import ngoy.core.TemplateCache;
import ngoy.core.cli.Cli;
import ngoy.core.internal.CmpRef;
import ngoy.core.internal.CoreInternalModule;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.DefaultInjector;
import ngoy.core.internal.Resolver;
import ngoy.internal.parser.ByteCodeTemplate;
import ngoy.internal.parser.Parser;
import ngoy.internal.scan.ClassScanner;
import ngoy.internal.script.NgoyScript;
import ngoy.internal.site.SiteRenderer;
import ngoy.router.RouterModule;
import ngoy.translate.TranslateModule;
import ngoy.translate.TranslateService;

/**
 * The main entry point to ngoy.
 * <p>
 * Use {@link #renderString(String, Context, OutputStream, Config...)} or
 * {@link #renderTemplate(String, Context, OutputStream, Config...) } for simple
 * templates.
 * <p>
 * Use {@link #app(Class)} to build an 'app' using components, directives, pipes
 * etc.
 *
 * @author krizz
 */
public class Ngoy<T> {

	/**
	 * Used to build an 'app'.
	 */
	public static class Builder<T> {
		private final Class<T> appRoot;
		private final Config config = new Config();
		private final List<Provider> providers = new ArrayList<>();
		private final List<Injector> injectors = new ArrayList<>();
		private final List<ModuleWithProviders<?>> modules = new ArrayList<>();
		private final List<String> packagePrefixes = new ArrayList<>();

		private Builder(Class<T> appRoot) {
			this.appRoot = appRoot;
		}

		/**
		 * Adds the given providers to the app.
		 *
		 * @param providers Providers to add
		 * @return this
		 */
		public Builder<T> providers(Provider... providers) {
			this.providers.addAll(asList(providers));
			return this;
		}

		/**
		 * Adds the given injectors to the app.
		 * <p>
		 * Additional injectors may be used to integrate another DI system into ngoy.
		 *
		 * @param injectors injectors
		 * @return this
		 */
		public Builder<T> injectors(Injector... injectors) {
			this.injectors.addAll(asList(injectors));
			return this;
		}

		/**
		 * Adds the given runtime module to the app.
		 * <p>
		 * Some modules like the {@link RouterModule} cannot be declared using
		 * annotations only. They need 'runtime' information, usually passed with a call
		 * to a static 'forRoot' method such as
		 * {@link RouterModule#forRoot(ngoy.router.RouterConfig)}.
		 *
		 * @param modules Modules to add
		 * @return this
		 */
		public Builder<T> modules(ModuleWithProviders<?>... modules) {
			this.modules.addAll(asList(modules));
			return this;
		}

		/**
		 * Loads all declarations/injectables that are part of the given packages.
		 * <p>
		 * This will load all classes that are part of the given packages.
		 * 
		 * @param packages Packages to load declarations/injectables from
		 * @return this
		 */
		public Builder<T> modules(Package... packages) {
			return modules(Stream.of(packages)
					.map(Package::getName)
					.collect(toList())
					.toArray(new String[0]));
		}

		/**
		 * Loads all declarations/injectables that are part of the given packages.
		 * <p>
		 * This will load all classes that are part of the given packages.
		 * 
		 * @param packages Packages to load declarations/injectables from, such as
		 *                 <code>org.myapp</code>
		 * @return this
		 */
		public Builder<T> modules(String... packages) {
			this.packagePrefixes.addAll(asList(packages));
			return this;
		}

		/**
		 * Whether to inline components.
		 * <p>
		 * In this mode, a component's (host) element is not written but only it's
		 * contents.
		 * 
		 * @param inlineComponents whether to inline components
		 * @return this
		 */
		public Builder<T> inlineComponents(boolean inlineComponents) {
			config.inlineComponents = inlineComponents;
			return this;
		}

		/**
		 * The content type.
		 * <p>
		 * Known values are: <code>text/xml</code>, <code>text/plain</code> and
		 * <code>text/html</code>. Default is <code>text/html</code>.
		 * <p>
		 * This affects the output escaping. With <code>text/plain</code>, no escaping
		 * takes place.
		 *
		 * @param contentType The content type
		 * @return this
		 */
		public Builder<T> contentType(String contentType) {
			config.contentType = contentType;
			return this;
		}

		/**
		 * The locale to use.
		 * <p>
		 * This affects i.e. formatting of the {@link DatePipe}
		 *
		 * @param locale Default is the system locale
		 * @return this
		 */
		public Builder<T> locale(Locale locale) {
			this.config.locale = locale;
			return this;
		}

		/**
		 * Load a translation bundle such as <code>messages</code>. If this member is
		 * set, the {@link TranslateModule} is automatically added to the app.
		 *
		 * @param translateBundle translation bundle such as <code>messages</code>. Same
		 *                        as you would pass to
		 *                        {@link PropertyResourceBundle#getBundle(String)}
		 * @return this
		 */
		public Builder<T> translateBundle(String translateBundle) {
			this.config.translateBundle = translateBundle;
			return this;
		}

		/**
		 * Builds the app instance, on which then {@link Ngoy#render(OutputStream)} can
		 * be called.
		 *
		 * @return App
		 */
		public Ngoy<T> build() {
			return new Ngoy<T>(appRoot, //
					config, //
					injectors, //
					modules, //
					packagePrefixes, //
					providers);
		}
	}

	/**
	 * Begins building an 'app' using components, pipes etc.
	 *
	 * @param appRoot The root component. The class must have at least the
	 *                {@link Component} annotation set. It may have the
	 *                {@link NgModule} annotation set if the app is using other
	 *                components
	 * @return A new builder
	 * @param <T> The type of the app
	 */
	public static <T> Builder<T> app(Class<T> appRoot) {
		return new Builder<T>(appRoot);
	}

	/**
	 * Renders the given string.
	 * <p>
	 * Example with variable:
	 *
	 * <pre>
	 * Ngoy.renderString("hello: {{name}}", Context.of("name", "peter"), System.out);
	 *
	 * &gt;&gt; hello peter
	 * </pre>
	 *
	 * Example with model:
	 *
	 * <pre>
	 * public class Person {
	 * 	private final String name;
	 *
	 * 	public Person(String name) {
	 * 		this.name = name;
	 * 	}
	 *
	 * 	public String getName() {
	 * 		return name;
	 * 	}
	 * }
	 *
	 * Ngoy.renderString("hello: {{name}}", Context.of(new Person("sam")), System.out);
	 *
	 * &gt;&gt; hello sam
	 *
	 * </pre>
	 *
	 * @param template The template
	 * @param context  Execution context used to provide variables and/or a 'model'
	 *                 to the template
	 * @param out      Where the processed template is written to
	 * @param config   Optional configuration
	 */
	public static void renderString(String template, Context context, OutputStream out, Config... config) {
		Config cfg = config.length > 0 ? config[0] : new Config();
		new Ngoy<Void>(cfg).render(template, cfg.templateIsExpression, context, out);
	}

	/**
	 * Renders the given template file/resource.
	 * <p>
	 * Same as {@link #renderString(String, Context, OutputStream, Config...)},
	 * except that the template is read from the given path.
	 *
	 * @param templatePath path to the template. The template resource is loaded
	 *                     with {@link Class#getResourceAsStream(String)}
	 * @param context      Execution context used to provide variables and/or a
	 *                     'model' to the template
	 * @param out          Where the processed template is written to
	 * @param config       Optional configuration
	 * @see #renderString(String, Context, OutputStream, Config...)
	 */
	public static void renderTemplate(String templatePath, Context context, OutputStream out, Config... config) {
		InputStream in = Ngoy.class.getResourceAsStream(templatePath);
		if (in == null) {
			throw new NgoyException("Template could not be found: '%s'", templatePath);
		}
		try (InputStream inn = in) {
			renderString(copyToString(inn), context, out, config);
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	/**
	 * Configuration used for simple templates.
	 */
	public static class Config {
		/**
		 * The locale to render the template with
		 */
		public Locale locale;

		/**
		 * Path to the translation bundle, such as <code>messages</code>.
		 */
		public String translateBundle;

		/**
		 * Whether to inline components. TODO: document
		 */
		public boolean inlineComponents;

		/**
		 * The content type. Known values are: <code>text/xml</code>,
		 * <code>text/plain</code> and <code>text/html</code>. Default is
		 * <code>text/html</code>.
		 * <p>
		 * This affects the output escaping. With <code>text/plain</code>, no escaping
		 * takes place.
		 */
		public String contentType;

		/**
		 * Whether to treat the template as an expression.
		 */
		public boolean templateIsExpression;
	}

	private final Config config;
	private final Class<T> appRoot;
	private T appInstance;
	private Resolver resolver;
	private DefaultInjector injector;
	private final TemplateCache cache = new TemplateCache();
	private final Events events = new Events();

	protected Ngoy(Config config) {
		this(Object.class, config, emptyList(), emptyList(), emptyList(), emptyList());
	}

	@SuppressWarnings("unchecked")
	protected Ngoy(Class<?> appRoot, Config config, List<Injector> injectors, List<ModuleWithProviders<?>> modules, List<String> packagePrefixes, List<Provider> rootProviders) {
		this.appRoot = (Class<T>) appRoot;
		this.config = config;
		this.init(injectors, modules, packagePrefixes, rootProviders);
	}

	private void init(List<Injector> injectors, List<ModuleWithProviders<?>> modules, List<String> packagePrefixes, List<Provider> rootProviders) {

		if (!packagePrefixes.isEmpty()) {
			modules.add(new ClassScanner() //
					.exclude(appRoot.getName())
					.scan(packagePrefixes.toArray(new String[packagePrefixes.size()])));
		}

		List<Provider> cmpProviders = new ArrayList<>();
		Map<String, List<Provider>> cmpDecls = new LinkedHashMap<>(); // order of css
		Map<String, Provider> pipeDecls = new HashMap<>();

		if (provides(LocaleProvider.class, rootProviders) == null) {
			cmpProviders.add(useValue(LocaleProvider.class, new LocaleProvider.Default(config.locale != null ? config.locale : Locale.getDefault())));
		}

		String translateBundle = config.translateBundle;
		boolean hasTranslateBundle = isSet(translateBundle);
		if (hasTranslateBundle) {
			addModuleDecls(TranslateModule.class, cmpDecls, pipeDecls, cmpProviders);
		}

		addModuleDecls(CoreInternalModule.class, cmpDecls, pipeDecls, cmpProviders);
		addModuleDecls(CommonModule.class, cmpDecls, pipeDecls, cmpProviders);
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
		all.add(useValue(Events.class, events));
		all.add(of(SiteRenderer.class));
		all.addAll(cmpProviders);
		cmpDecls.values()
				.stream()
				.flatMap(List::stream)
				.forEach(all::add);
		all.addAll(pipeDecls.values());
		all.addAll(rootProviders);

		injector = new DefaultInjector(injectors.toArray(new Injector[injectors.size()]), all.toArray(new Provider[all.size()]));

		initAppInstance(rootProviders);

		if (hasTranslateBundle) {
			injector.get(TranslateService.class)
					.setBundle(translateBundle);
		}
	}

	@SuppressWarnings("unchecked")
	private void initAppInstance(List<Provider> rootProviders) {
		Provider appRootProvider = provides(appRoot, rootProviders);
		if (appRootProvider != null && appRootProvider.getUseValue() != null) {
			appInstance = (T) appRootProvider.getUseValue();
			injector.injectFields(appRoot, appInstance, new HashSet<>());
		} else {
			injector.put(of(appRoot));
			appInstance = (T) injector.get(appRoot);
		}
	}

	private List<Provider> toProviders(List<Class<?>> list) {
		return list.stream()
				.map(Provider::of)
				.collect(toList());
	}

	private Resolver createResolver(Map<String, List<Provider>> cmpDecls, Map<String, Provider> pipeDecls) {
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
						.flatMap(List::stream)
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
						.flatMap(List::stream)
						.map(Provider::getProvide)
						.forEach(all::add);
				return all;
			}
		};
	}

	private Provider provides(Class<?> theClass, List<Provider> rootProviders) {
		return rootProviders.stream()
				.filter(p -> p.getProvide()
						.equals(theClass))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Renders the site/page to the given folder.
	 * <p>
	 * If a {@link RouterModule} is present, renders a page for each configured
	 * route.
	 *
	 * @param folder target folder. Subdirectories are created as needed
	 */
	public void renderSite(Path folder) {
		injector.get(SiteRenderer.class)
				.render(this, folder);
	}

	private void render(String template, boolean templateIsExpr, Context context, OutputStream out) {
		try {

			Object app = context == null ? appInstance : context.getModel();

			if (app instanceof OnInit) {
				((OnInit) app).ngOnInit();
			}

			Ctx ctx = Ctx.of(app, injector);
			if (context != null) {
				for (Map.Entry<String, Object> v : context.getVariables()
						.entrySet()) {
					ctx.variable(v.getKey(), v.getValue());
				}
			}

			events.tick();

			if (templateIsExpr) {
				NgoyScript script = new NgoyScript(resolver);
				Object result = script.run(template, ctx);
				newPrintStream(out).print(result);
			} else {
				parseAndRender(appRoot, template, createParser(resolver, config), ctx, newPrintStream(out));
			}

			if (app instanceof OnDestroy) {
				((OnDestroy) app).ngOnDestroy();
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	/**
	 * Renders the app to the given ouput stream.
	 *
	 * @param out To where to write the app to
	 */
	public void render(OutputStream out) {
		render(null, false, null, out);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addModuleDecls(Class<?> clazz, Map<String, List<Provider>> targetCmps, Map<String, Provider> targetPipes, List<Provider> providers) {
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

		Directive dir = clazz.getAnnotation(Directive.class);
		if (dir != null) {

			for (Class<?> prov : dir.providers()) {
				providers.add(of(prov));
			}

			for (Provide prov : dir.provide()) {
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

	private void addDecls(List<Provider> providers, Map<String, List<Provider>> targetCmps, Map<String, Provider> targetPipes) {
		for (Provider p : providers) {
			Pipe pipe = p.getProvide()
					.getAnnotation(Pipe.class);
			if (pipe != null) {
				targetPipes.put(pipe.value(), p);
			}

			Component cmp = p.getProvide()
					.getAnnotation(Component.class);
			if (cmp != null) {
				putCmp(cmp.selector(), p, targetCmps, false);
			}

			Directive dir = p.getProvide()
					.getAnnotation(Directive.class);
			if (dir != null) {
				putCmp(dir.selector(), p, targetCmps, true);
			}
		}
	}

	private void putCmp(String selector, Provider p, Map<String, List<Provider>> targetCmps, boolean allowMulti) {
		List<Provider> list = targetCmps.get(selector);
		if (list != null && !allowMulti) {
			Provider existing = list.get(0);
			throw new NgoyException("More than one component matched on the selector '%s'. Make sure that only one component's selector can match a given element. Conflicting components: %s, %s",
					selector, existing.getProvide()
							.getName(),
					p.getProvide()
							.getName());
		}

		if (list == null) {
			list = new ArrayList<>();
			targetCmps.put(selector, list);
		}
		list.add(p);
	}

	protected void parseAndRender(Class<T> appRoot, String template, Parser parser, Ctx ctx, PrintStream out) {
		invokeRender(cache.get(appRoot.getName(), className -> createTemplate(className, parser, template != null ? template : getTemplate(appRoot), getContentType(config))), ctx, out);
	}

	private void invokeRender(Class<?> templateClass, Ctx ctx, PrintStream out) {
		try {
			ctx.setOut(out, getContentType(config));
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

	private String getContentType(Config config) {
		String contentType = config.contentType;
		if ((contentType == null || contentType.isEmpty()) && appRoot != null) {
			Component cmp = appRoot.getAnnotation(Component.class);
			if (cmp != null) {
				contentType = cmp.contentType();
			}
		}
		return contentType;
	}

	private Parser createParser(@Nullable Resolver r, Config config) {
		Parser parser = new Parser(r);
		parser.contentType = getContentType(config);
		parser.inlineComponents = config.inlineComponents;
		parser.inlineAll = "text/plain".equals(parser.contentType);
		return parser;
	}

	/**
	 * experimental.
	 *
	 * @param event   The 'token'
	 * @param payload payload
	 * @param         <E> Type of payload
	 * @return this
	 */
	public <E> Ngoy<?> publish(Object event, E payload) {
		events.publish(event, payload);
		return this;
	}

	/**
	 * Destroys the app.
	 * <p>
	 * Once called, the app must be built from scratch.
	 */
	public void destroy() {
		appInstance = null;
	}

	/**
	 * Returns the app instance.
	 *
	 * @return app instance. Null if the app has been destroyed.
	 */
	@Nullable
	public T getAppInstance() {
		return appInstance;
	}

	/**
	 * The entry point for the {@link Cli}.
	 *
	 * @param args arguments
	 */
	public static void main(String[] args) {
		new Cli().run(args, System.out);
	}

	private static boolean matchesAttributeBinding(Jerry node, String attrName) {
		// directive name same as @Input
		String raw = attrName.substring(1, attrName.length() - 1);
		return node.is(format("[\\[%s\\]]", raw));
	}
}
