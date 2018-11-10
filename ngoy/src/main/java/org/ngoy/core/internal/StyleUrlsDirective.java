package org.ngoy.core.internal;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.ngoy.core.NgoyException.wrap;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.jsoup.nodes.Element;
import org.ngoy.core.Component;
import org.ngoy.core.Directive;
import org.ngoy.core.ElementRef;
import org.ngoy.core.Inject;
import org.ngoy.core.NgoyException;
import org.ngoy.core.OnCompile;
import org.ngoy.core.Util;

@Directive(selector = "html")
public class StyleUrlsDirective implements OnCompile {
	@Inject
	public Resolver resolver;

	@Override
	public void ngOnCompile(ElementRef elRef, String cmpClass) {
		try {
			Element el = ((JSoupElementRef) elRef).getNativeElement();

			Set<Class<?>> cmpClasses = resolver.getCmpClasses();
			String styles = cmpClasses.stream()
					.map(this::getStyles)
					.collect(joining("\n"));

			Files.write(Paths.get("d:/downloads/x.css"), styles.getBytes());

			Element styleEl = el.selectFirst("style");
			if (styleEl == null) {
				findParent(el).appendChild(el.ownerDocument()
						.createElement("style")
						.attr("type", "text/css")
						.text(styles));
			} else {
				String existingStyles = styleEl.childNodes()
						.stream()
						.map(Object::toString)
						.collect(joining(""));
				styleEl.text(format("%s\n%s", existingStyles, styles));
			}

		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private String getStyles(Class<?> clazz) {
		return Optional.ofNullable(clazz.getAnnotation(Component.class))
				.map(a -> Stream.of(a.styleUrls())
						.filter(url -> !url.isEmpty())
						.map(url -> {
							InputStream in = clazz.getResourceAsStream(url);
							if (in == null) {
								throw new NgoyException("Style could not be found: '%s'", url);
							}
							String style;
							try (InputStream inn = in) {
								style = Util.copyToString(inn);
							} catch (Exception e) {
								throw wrap(e);
							}

							return style;
						})
						.collect(joining("\n")))
				.orElse("");

	}

	private Element findParent(Element el) {
		Element parent = el.selectFirst("head");
		if (parent == null) {
			parent = el.selectFirst("body");
		}
		if (parent == null) {
			parent = el;
		}
		return parent;
	}

}
