package org.ngoy.router;

import org.ngoy.core.Directive;
import org.ngoy.core.HostBinding;
import org.ngoy.core.Input;

@Directive(selector = "[routerLink]")
public class RouterLinkDirective {

	@Input
	public String routerLink;

	@HostBinding("attr.href")
	public String href() {
		return routerLink;
	}
}
