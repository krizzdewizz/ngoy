package ngoy.router;

import ngoy.core.Directive;
import ngoy.core.HostBinding;
import ngoy.core.Inject;
import ngoy.core.Input;
import ngoy.internal.site.SiteRenderer;

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

	@Inject
	public SiteRenderer siteRenderer;

	@HostBinding("attr.href")
	public String href() {
		return siteRenderer.toStaticLink(routerLink);
	}
}
