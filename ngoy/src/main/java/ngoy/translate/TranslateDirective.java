package ngoy.translate;

import ngoy.core.Directive;
import ngoy.core.HostBinding;
import ngoy.core.Inject;
import ngoy.core.Input;

@Directive(selector = "[translate]")
public class TranslateDirective {

	@Inject
	public TranslateService translateService;

	@HostBinding("text")
	public String translation;

	@Input
	public void translate(String key) {
		translation = translateService.translate(key);
	}
}
