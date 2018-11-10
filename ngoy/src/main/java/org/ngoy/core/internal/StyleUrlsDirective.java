package org.ngoy.core.internal;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.ngoy.core.NgoyException.wrap;
import static org.ngoy.core.Util.copyToString;

import java.io.InputStream;
import java.util.stream.Stream;

import org.jsoup.nodes.Element;
import org.ngoy.core.Component;
import org.ngoy.core.Directive;
import org.ngoy.core.ElementRef;
import org.ngoy.core.NgoyException;
import org.ngoy.core.OnCompile;

@Directive(selector = "html")
public class StyleUrlsDirective implements OnCompile {

	@Override
	public void ngOnCompile(ElementRef elRef, String cmpClass) {
		try {
			Element el = ((JSoupElementRef) elRef).getNativeElement();

			Class<?> clazz = Thread.currentThread()
					.getContextClassLoader()
					.loadClass(cmpClass);

			String[] urls = clazz.getAnnotation(Component.class)
					.styleUrls();

			String styles = Stream.of(urls)
					.map(url -> {
						InputStream in = clazz.getResourceAsStream(url);
						if (in == null) {
							throw new NgoyException("Style could not be found: '%s'", url);
						}
						String style;
						try (InputStream inn = in) {
							style = copyToString(inn);
						} catch (Exception e) {
							throw wrap(e);
						}

						return style;
					})
					.collect(joining("\n"));

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
