package ngoy;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Provider.of;
import static ngoy.core.Provider.useClass;
import static ngoy.core.Provider.useValue;
import static ngoy.core.Util.copyToString;
import static ngoy.core.Util.getCompileExceptionMessageWithoutLocation;
import static ngoy.core.Util.getTemplate;
import static ngoy.core.Util.isSet;
import static ngoy.internal.parser.template.JavaTemplate.getExprComment;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.ClassBodyEvaluator;

import jodd.jerry.Jerry;
import ngoy.common.CommonModule;
import ngoy.common.DatePipe;
import ngoy.core.AppRoot;
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
import ngoy.core.Pipe;
import ngoy.core.Provide;
import ngoy.core.Provider;
import ngoy.core.cli.Cli;
import ngoy.core.internal.CmpRef;
import ngoy.core.internal.CoreInternalModule;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.Debug;
import ngoy.core.internal.DefaultInjector;
import ngoy.core.internal.Output;
import ngoy.core.internal.Resolver;
import ngoy.core.internal.StyleUrlsDirective;
import ngoy.core.internal.TemplateRender;
import ngoy.core.internal.WriterOutput;
import ngoy.internal.parser.Parser;
import ngoy.internal.parser.template.JavaTemplate;
import ngoy.internal.parser.template.JavaTemplate.ExprComment;
import ngoy.internal.scan.ClassScanner;
import ngoy.internal.site.SiteRenderer;
import ngoy.router.RouterModule;
import ngoy.translate.TranslateModule;
import ngoy.translate.TranslateService;

/**
 * Main entry point to ngoy.
 * <p>
 * Use {@link #renderString(String, Context, Writer, Config...)} or
 * {@link #renderTemplate(String, Context, Writer, Config...) } for simple
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
		private final Class<T> appClass;
		private final Config config = new Config();
		private final List<Provider> providers = new ArrayList<>();
		private final List<Injector> injectors = new ArrayList<>();
		private final List<ModuleWithProviders<?>> modules = new ArrayList<>();
		private final List<String> packagePrefixes = new ArrayList<>();

		private Builder(Class<T> appClass) {
			this.appClass = appClass;
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
					.toArray(String[]::new));
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
		 * Whether to prefix css style rules with the component's selector (element
		 * name).
		 *
		 * @param prefixCss true to prefix
		 * @return this
		 */
		public Builder<T> prefixCss(boolean prefixCss) {
			config.prefixCss = prefixCss;
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
		 * Builds the app instance, on which then {@link Ngoy#render(Writer)} can be
		 * called.
		 *
		 * @return App
		 */
		public Ngoy<T> build() {
			return new Ngoy<T>(appClass, //
					null, //
					config, //
					injectors, //
					modules, //
					packagePrefixes, //
					providers, //
					null);
		}
	}

	/**
	 * Begins building an 'app' using components, pipes etc.
	 *
	 * @param appClass The class or the root component. The class must have at least
	 *                 the {@link Component} annotation set. It may have the
	 *                 {@link NgModule} annotation set if the app is using other
	 *                 components
	 * @return A new builder
	 * @param <T> The type of the app
	 */
	public static <T> Builder<T> app(Class<T> appClass) {
		return new Builder<T>(appClass);
	}

	public static void renderString(String template, Context<?> context, OutputStream out, Config... config) {
		OutputStreamWriter writer = newOutputStreamWriter(out);
		try {
			renderString(template, context, writer, config);
		} finally {
			try {
				writer.flush();
			} catch (Exception e) {
				throw wrap(e);
			}
		}
	}

	public static void renderTemplate(String template, Context<?> context, OutputStream out, Config... config) {
		OutputStreamWriter writer = newOutputStreamWriter(out);
		try {
			renderTemplate(template, context, writer, config);
		} finally {
			try {
				writer.flush();
			} catch (Exception e) {
				throw wrap(e);
			}
		}
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
	public static void renderString(String template, Context<?> context, Writer out, Config... config) {
		Config cfg = config.length > 0 ? config[0] : new Config();
		new Ngoy<Void>(template, cfg, context).render(out);
	}

	/**
	 * Renders the given template file/resource.
	 * <p>
	 * Same as {@link #renderString(String, Context, Writer, Config...)}, except
	 * that the template is read from the given path.
	 *
	 * @param templatePath path to the template. The template resource is loaded
	 *                     with {@link Class#getResourceAsStream(String)}
	 * @param context      Execution context used to provide variables and/or a
	 *                     'model' to the template
	 * @param out          Where the processed template is written to
	 * @param config       Optional configuration
	 * @see #renderString(String, Context, Writer, Config...)
	 */
	public static void renderTemplate(String templatePath, Context<?> context, Writer out, Config... config) {
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

		/**
		 * If true, prefixes a component's style rule with it's selector.
		 */
		public boolean prefixCss;
	}

	private final Config config;
	private final Class<T> appClass;
	private T appInstance;
	private TemplateRender templateRenderer;
	private Resolver resolver;
	private DefaultInjector injector;
	private final Events events = new Events();
	private final String template;
	private final Map<String, Provider> pipeDecls = new HashMap<>();
	private final Context<?> context;
	private String contentType;
	private boolean contentTypeComputed;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Ngoy(String template, Config config, Context context) {
		this(context.getModel() != null ? context.getModelClass() : Object.class, template, config, emptyList(), emptyList(), emptyList(),
				context.getModel() != null ? asList(useValue(context.getModelClass(), context.getModel())) : emptyList(), context);
	}

	@SuppressWarnings("unchecked")
	protected Ngoy(Class<?> appClass, String template, Config config, List<Injector> injectors, List<ModuleWithProviders<?>> modules, List<String> packagePrefixes, List<Provider> rootProviders,
			Context<?> context) {
		this.context = context;
		this.template = config.templateIsExpression ? template : null;
		this.appClass = (Class<T>) appClass;
		this.config = config;
		init(injectors, modules, packagePrefixes, rootProviders);
		compile(template);
	}

	private void init(List<Injector> injectors, List<ModuleWithProviders<?>> modules, List<String> packagePrefixes, List<Provider> rootProviders) {

		if (!packagePrefixes.isEmpty()) {
			modules.add(new ClassScanner() //
					.excludeClassNames(appClass.getName())
					.scan(packagePrefixes.toArray(new String[packagePrefixes.size()])));
		}

		List<Provider> cmpProviders = new ArrayList<>();
		Map<String, List<Provider>> cmpDecls = new LinkedHashMap<>(); // order of css

		if (provides(LocaleProvider.class, rootProviders) == null) {
			cmpProviders.add(useValue(LocaleProvider.class, new LocaleProvider.Default(config.locale != null ? config.locale : Locale.getDefault())));
		}

		String translateBundle = config.translateBundle;
		boolean hasTranslateBundle = isSet(translateBundle);
		if (hasTranslateBundle) {
			addModuleDecls(TranslateModule.class, cmpDecls, pipeDecls, cmpProviders);
		}

		if (config.prefixCss) {
			cmpProviders.add(useValue(StyleUrlsDirective.Config.class, new StyleUrlsDirective.Config(null, true)));
		}

		addModuleDecls(CoreInternalModule.class, cmpDecls, pipeDecls, cmpProviders);
		addModuleDecls(CommonModule.class, cmpDecls, pipeDecls, cmpProviders);
		addModuleDecls(appClass, cmpDecls, pipeDecls, cmpProviders);

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
		Set<Class<?>> cmpDeclsSet = new HashSet<>();

		all.add(useValue(Resolver.class, resolver));
		all.add(useValue(Events.class, events));
		all.add(of(SiteRenderer.class));
		all.addAll(cmpProviders);
		cmpDecls.values()
				.stream()
				.flatMap(List::stream)
				.forEach(decl -> {
					all.add(decl);
					cmpDeclsSet.add(decl.getProvide());
				});
		all.addAll(pipeDecls.values());
		all.addAll(rootProviders);

		injector = new DefaultInjector(cmpDeclsSet, injectors.toArray(new Injector[injectors.size()]), all.toArray(new Provider[all.size()]));

		initAppInstance(rootProviders);

		if (hasTranslateBundle) {
			injector.get(TranslateService.class)
					.setBundle(translateBundle);
		}
	}

	@SuppressWarnings("unchecked")
	private void initAppInstance(List<Provider> rootProviders) {
		Provider appClassProvider = provides(appClass, rootProviders);
		if (appClassProvider != null && appClassProvider.getUseValue() != null) {
			appInstance = (T) appClassProvider.getUseValue();
			injector.applyInjections(appInstance, injector.fieldInjections(appClass, new HashSet<>()));
		} else {
			injector.put(of(appClass));
			appInstance = injector.get(appClass);
		}
		injector.put(Provider.useValue(AppRoot.class, new AppRoot() {
			@Override
			public Class<?> getAppClass() {
				return appClass;
			}

			@Override
			public <A> A getAppInstance() {
				return (A) appInstance;
			}
		}));
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
				return cmpClass != null ? cmpClass : appClass;
			}

			@Override
			public Set<Class<?>> getCmpClasses() {
				Set<Class<?>> all = new LinkedHashSet<>();
				all.add(appClass);
				cmpDecls.values()
						.stream()
						.flatMap(List::stream)
						.map(Provider::getProvide)
						.forEach(all::add);
				return all;
			}

			@Override
			public Class<?> getAppClass() {
				return appClass;
			}

			@Override
			public List<Class<?>> resolvePipes() {
				return pipeDecls.values()
						.stream()
						.map(Provider::getProvide)
						.collect(toList());
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
				.render(this, folder, () -> compile(template));
	}

	private Ctx createRenderContext() {
		Ctx ctx = Ctx.of(injector);

		if (context != null) {
			ctx.setVariables(context.getVariables());
		}

		events.tick();
		return ctx;
	}

	/**
	 * Renders the app to the given ouput stream using UTF-8 encoding.
	 *
	 * @param out To where to write the app to
	 */
	public void render(OutputStream out) {
		Writer writer = newOutputStreamWriter(out);
		try {
			render(writer);
		} finally {
			try {
				writer.flush();
			} catch (Exception e) {
				throw wrap(e);
			}
		}
	}

	private static OutputStreamWriter newOutputStreamWriter(OutputStream out) {
		return new OutputStreamWriter(out, StandardCharsets.UTF_8);
	}

	/**
	 * Renders the app to the given writer.
	 *
	 * @param out To where to write the app to
	 */
	public void render(Writer out) {
		render(createRenderContext(), new WriterOutput(out));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addModuleDecls(Class<?> mod, Map<String, List<Provider>> targetCmps, Map<String, Provider> targetPipes, List<Provider> providers) {

		NgModule ngMod = mod.getAnnotation(NgModule.class);
		if (ngMod != null) {
			addDecls(toProviders(asList(ngMod.declarations())), targetCmps, targetPipes);

			for (Class<?> imp : ngMod.imports()) {
				addModuleDecls(imp, targetCmps, targetPipes, providers);
			}

			for (Class<?> prov : ngMod.providers()) {
				providers.add(of(prov));
			}

			for (Provide prov : ngMod.provide()) {
				Class p = prov.provide();
				Class c = prov.useClass();
				providers.add(useClass(p, c));
			}
		}

		Component cmp = mod.getAnnotation(Component.class);
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
			if (existing.getProvide() == p.getProvide()) {
				// double registration on same component
				return;
			}
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

	private void compile(String template) {
		try {
			Parser parser = createParser(resolver, config);
			Class<?> templateClass = compileTemplate(parser, template != null ? template : getTemplate(appClass));
			templateRenderer = (TemplateRender) templateClass.getMethod("createRenderer", Injector.class)
					.invoke(null, resolver.getInjector());
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private void render(Ctx ctx, Output out) {
		try {
			ctx.setOut(out);
			templateRenderer.render(ctx);
		} catch (Exception e) {
			throw wrap(e);
		} finally {
			ctx.resetOut();
		}
	}

	protected Class<?> compileTemplate(Parser parser, String template) {
		JavaTemplate tpl = new JavaTemplate(getContentType(config), context != null ? context.getVariables() : emptyMap());
		parser.parse(template, tpl);
		String code = tpl.toString();

		Debug.writeTemplate(code);

		ClassBodyEvaluator bodyEvaluator = new ClassBodyEvaluator();
		try {
			bodyEvaluator.cook(code);
		} catch (CompileException e) {
			Location location = e.getLocation();
			ExprComment exprComment = location == null ? new ExprComment("<unknown>", "") : getExprComment(code, location.getLineNumber());

			String msg = getCompileExceptionMessageWithoutLocation(e);
			throw new NgoyException("Compile error in \"%s\": %s\nsource: %s", exprComment.comment, msg, exprComment.sourcePosition);
		}
		return bodyEvaluator.getClazz();
	}

	private String getContentType(Config config) {
		if (contentTypeComputed) {
			return contentType;
		}
		String contentType = config.contentType;
		if ((contentType == null || contentType.isEmpty()) && appClass != null) {
			Component cmp = appClass.getAnnotation(Component.class);
			if (cmp != null) {
				contentType = cmp.contentType();
			}
		}
		contentTypeComputed = true;
		return this.contentType = contentType;
	}

	private Parser createParser(@Nullable Resolver resolver, Config config) {
		Parser parser = new Parser(resolver);
		parser.contentType = getContentType(config);
		parser.inlineComponents = config.inlineComponents;
		parser.inlineAll = "text/plain".equals(parser.contentType);
		parser.templateIsExpression = config.templateIsExpression;
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
	public static void main(String[] args) throws Exception {
		OutputStreamWriter out = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
		try {
			new Cli().run(args, out);
		} finally {
			out.flush();
		}
	}

	private static boolean matchesAttributeBinding(Jerry node, String attrName) {
		// directive name same as @Input
		String raw = attrName.substring(1, attrName.length() - 1);
		return node.is(format("[\\[%s\\]]", raw));
	}
}
