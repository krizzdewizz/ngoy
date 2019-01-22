package ngoy.core.internal;

import ngoy.core.NgoyException;

/**
 * Exception thrown from the generated TemplateRender class.
 * 
 * @author krizz
 */
public class RenderException extends NgoyException {

	private static final long serialVersionUID = 1L;

	public final String debugInfo;

	public RenderException(Exception cause, String debugInfo) {
		super(cause);
		this.debugInfo = debugInfo;
	}
}
