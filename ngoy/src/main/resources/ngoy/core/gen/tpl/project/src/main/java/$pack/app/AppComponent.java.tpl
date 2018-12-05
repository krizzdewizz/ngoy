package {{pack}}.app;

import ngoy.core.Component;
import ngoy.core.NgModule;

@Component(selector = "", templateUrl = "app.component.html", styleUrls = { "app.component.css" })
@NgModule(declarations = {}, providers = {})
public class AppComponent {
	public final String title = "{{ name }}-app";
}
