package org.ngoy.core;

public abstract class ElementRef {
	private final Object nativeElement;

	public ElementRef(Object nativeElement) {
		this.nativeElement = nativeElement;
	}

	public abstract boolean is(String selector);

	@SuppressWarnings("unchecked")
	public <T> T getNativeElement() {
		return (T) nativeElement;
	}

	@Override
	public String toString() {
		return getNativeElement().toString();
	}
}
