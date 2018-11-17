package ngoy.router;

import ngoy.core.Directive;
import ngoy.core.HostBinding;
import ngoy.core.Input;

@Directive(selector = "[routerLink]")
public class RouterLinkDirective {

	@Input
	public String routerLink;

	@HostBinding("attr.href")
	public String href() {
		return routerLink;
	}
}
