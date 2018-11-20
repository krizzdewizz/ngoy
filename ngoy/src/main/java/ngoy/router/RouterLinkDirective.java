package ngoy.router;

import ngoy.core.Directive;
import ngoy.core.HostBinding;
import ngoy.core.Input;

/**
 * Binds the <code>routerLink</code> attribute (path) to the <code>href</code>
 * attribute.
 * 
 * @author krizz
 */
@Directive(selector = "[routerLink]")
public class RouterLinkDirective {

	@Input
	public String routerLink;

	@HostBinding("attr.href")
	public String href() {
		return routerLink;
	}
}
