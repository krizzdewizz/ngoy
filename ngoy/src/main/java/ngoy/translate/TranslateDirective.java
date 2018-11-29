package ngoy.translate;

import ngoy.core.Directive;
import ngoy.core.HostBinding;
import ngoy.core.Inject;
import ngoy.core.Input;

/**
 * When applied, replaces the element's text content with the translation.
 * <p>
 * Example:
 * <p>
 * Given a message bundle:
 * 
 * <pre>
 * HELLO_MSG = hello
 * </pre>
 * 
 * then
 * 
 * <pre>
 * &lt;a translate="MSG_HELLO"&gt;&lt;/a&gt;
 * </pre>
 * 
 * will produce
 * 
 * <pre>
 * &lt;a&gt;hello&lt;/a&gt;
 * </pre>
 * 
 * As with any other attribute/directive, a binding can be used to get the
 * translation key dynamically:
 * 
 * <pre>
 * &lt;a [translate]="key"&gt;&lt;/a&gt;
 * </pre>
 * 
 * @author krizz
 */
@Directive(selector = "[translate]")
public class TranslateDirective {

	private TranslateService translateService;

	@Inject
	public void setTranslateService(TranslateService s) {
		translateService = s;
	}

	@HostBinding("ngText")
	public String translation;

	@Input
	public void translate(String key) {
		translation = key == null ? null : translateService.translate(key);
	}
}
