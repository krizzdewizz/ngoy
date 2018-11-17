package ngoy.core.internal;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Util.copyToString;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import jodd.jerry.Jerry;
import ngoy.core.Component;
import ngoy.core.Directive;
import ngoy.core.Inject;
import ngoy.core.NgoyException;
import ngoy.core.OnCompile;
import ngoy.internal.parser.visitor.XDom;

@Directive(selector = "html")
public class StyleUrlsDirective implements OnCompile {
	@Inject
	public Resolver resolver;

	@Override
	public void ngOnCompile(Jerry el, String cmpClass) {
		try {
			Set<Class<?>> cmpClasses = resolver.getCmpClasses();
			String styles = cmpClasses.stream()
					.map(this::getStyles)
					.filter(style -> !style.isEmpty())
					.collect(joining("\n"));

			Jerry styleEl = el.$("style");
			if (styleEl.length() == 0) {
				Jerry ell = XDom.createElement("style");
				ell.attr("type", "text/css");
				ell.text(styles);
				XDom.appendChild(findParent(el), ell);
			} else {
				StringBuilder existingStyles = new StringBuilder();
				styleEl.contents()
						.forEach(s -> existingStyles.append(s.text()));
				styleEl.text(format("%s\n%s", existingStyles, styles));
			}

		} catch (Exception e) {
			throw wrap(e);
		}
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