package org.ngoy.core.internal;

import org.jsoup.nodes.Element;
import org.ngoy.core.ElementRef;

public class JSoupElementRef extends ElementRef {

	public JSoupElementRef(Element el) {
		super(el);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Element getNativeElement() {
		return super.getNativeElement();
	}

	@Override
	public boolean is(String selector) {
		return getNativeElement().is(selector);
	}
}
