package org.ngoy.translate;

import org.ngoy.core.Directive;
import org.ngoy.core.HostBinding;
import org.ngoy.core.Inject;
import org.ngoy.core.Input;
import org.ngoy.core.OnInit;

@Directive(selector = "[translate]")
public class TranslateDirective implements OnInit {

	@Inject
	public TranslateService translateService;

	@HostBinding("text")
	public String translation;

	@Input
	public void translate(String key) {
		translation = translateService.translate(key);
	}

	@Override
	public void ngOnInit() {
	}
}
