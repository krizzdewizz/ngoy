package ngoy.core.internal;

import static java.util.stream.Collectors.joining;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Util.copyToString;
import static ngoy.core.Util.isSet;
import static ngoy.core.dom.XDom.appendChild;
import static ngoy.core.dom.XDom.createElement;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

import jodd.jerry.Jerry;
import ngoy.core.Component;
import ngoy.core.Directive;
import ngoy.core.Inject;
import ngoy.core.NgoyException;
import ngoy.core.OnCompile;

@Directive(selector = "html")
public class StyleUrlsDirective implements OnCompile {
	@Inject
	public Resolver resolver;

	public String href;

	@Override
	public void ngOnCompile(Jerry el, String componentClass) {
		try {
			String styles = getStyles();

			if (styles.isEmpty()) {
				return;
			}

			if (isSet(href)) {
				Jerry lnk = appendChild(findParent(el), createElement("link", el));
				lnk.attr("rel", "stylesheet");
				lnk.attr("type", "text/css");
				lnk.attr("href", href);
			} else {
				Jerry st = appendChild(findParent(el), createElement("style", el));
				st.attr("type", "text/css");
				st.text(styles);
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public String getStyles() {
		return resolver.getCmpClasses()
				.stream()
				.map(this::getStyles)
				.filter(style -> !style.isEmpty())
				.collect(joining("\n"))
				.trim();
	}

	private String getStyles(Class<?> clazz) {
		return Optional.ofNullable(clazz.getAnnotation(Component.class))
				.map(ann -> Stream.of(ann.styleUrls())
						.filter(url -> !url.isEmpty())
						.map(url -> {
							InputStream in = clazz.getResourceAsStream(url);
							if (in == null) {
								throw new NgoyException("Style could not be found: '%s'. Component: %s", url, clazz.getName());
							}
							String style;
							try (InputStream inn = in) {
								style = copyToString(inn);
							} catch (Exception e) {
								throw wrap(e);
							}

							return style;
						})
						.collect(joining("\n")))
				.orElse("");

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

}
