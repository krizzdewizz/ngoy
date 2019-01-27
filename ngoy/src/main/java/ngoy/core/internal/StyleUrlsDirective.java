package ngoy.core.internal;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Util.copyToString;
import static ngoy.core.Util.isSet;
import static ngoy.core.dom.XDom.appendChild;
import static ngoy.core.dom.XDom.createElement;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Node;
import ngoy.core.Component;
import ngoy.core.Directive;
import ngoy.core.Inject;
import ngoy.core.MinifyCss;
import ngoy.core.NgoyException;
import ngoy.core.OnCompile;
import ngoy.core.OnCompileStyles;
import ngoy.core.Optional;

@Directive(selector = "html")
public class StyleUrlsDirective implements OnCompile {

	private static final Pattern CSS_RULE_PATTERN = Pattern.compile("(.*)\\{");

	public static class Config {
		public Config(String href, boolean prefix) {
			this.href = href;
			this.prefix = prefix;
		}

		public String href;
		public boolean prefix;
	}

	@Inject
	public Resolver resolver;

	@Inject
	@Optional
	public Config config = new Config(null, false);

	@Inject
	@Optional
	public MinifyCss minifyCss = Objects::requireNonNull;

	@Override
	public void onCompile(Jerry el, String componentClass) {
		try {
			String styles = getStyles();

			if (styles.isEmpty()) {
				return;
			}

			styles = minifyCss.minifyCss(styles);

			if (isSet(config.href)) {
				Jerry lnk = append(findParent(el), createElement("link", el));
				lnk.attr("rel", "stylesheet");
				lnk.attr("type", "text/css");
				lnk.attr("href", config.href);
			} else {
				Jerry st = append(findParent(el), createElement("style", el));
				st.attr("type", "text/css");
				st.text(styles);
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private Jerry append(Jerry parent, Jerry el) {
		Node ell = el.get(0);
		if ("head".equalsIgnoreCase(ell.getNodeName())) {
			return appendChild(parent, el);
		}

		Node parentt = parent.get(0);
		Node[] childNodes = parentt.getChildNodes();
		if (childNodes.length == 0) {
			return appendChild(parent, el);
		}

		parentt.insertBefore(ell, childNodes[0]);
		return el;
	}

	public String getStyles() {
		return resolver.getCmpClasses()
				.stream()
				.map(this::getStyles)
				.flatMap(List::stream)
				.filter(style -> !style.isEmpty())
				.collect(joining("\n"))
				.trim();
	}

	private String readStyles(Class<?> clazz, String url) {
		InputStream in = clazz.getResourceAsStream(url);
		if (in == null) {
			throw new NgoyException("Style resource could not be found: '%s'. Component: %s", url, clazz.getName());
		}
		String style;
		try (InputStream inn = in) {
			style = copyToString(inn);
		} catch (Exception e) {
			throw wrap(e);
		}

		return style;
	}

	private List<String> getStyles(Class<?> clazz) {
		Component cmp = clazz.getAnnotation(Component.class);
		if (cmp == null) {
			return emptyList();
		}

		String selector = cmp.selector();

		List<String> styles = Stream.of(cmp.styleUrls())
				.filter(url -> !url.isEmpty())
				.map(url -> readStyles(clazz, url))
				.map(style -> addPrefix(style, selector))
				.collect(toList());

		for (String style : cmp.styles()) {
			styles.add(addPrefix(style, selector));
		}

		if (OnCompileStyles.class.isAssignableFrom(clazz)) {
			OnCompileStyles compileStyle = (OnCompileStyles) resolver.getInjector()
					.get(clazz);
			styles.add(compileStyle.onCompileStyles());
		}

		return styles;
	}

	private Jerry findParent(Jerry el) {
		Jerry parent = el.$("head");

		if (parent.length() == 0) {
			parent = el.$("body");
		}
		if (parent.length() == 0) {
			parent = el;
		}
		return parent;
	}

	private String addPrefix(String css, String prefix) {
		if (!config.prefix || prefix.isEmpty()) {
			return css;
		}
		Matcher matcher = CSS_RULE_PATTERN.matcher(css);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String head = matcher.group(1);

			String newHead = format("%s %s{", prefix, head);
			matcher.appendReplacement(sb, newHead);
		}
		matcher.appendTail(sb);

		return sb.toString();
	}
}
