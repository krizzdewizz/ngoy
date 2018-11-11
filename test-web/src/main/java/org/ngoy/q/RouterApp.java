package org.ngoy.q;

import org.ngoy.core.Component;
import org.ngoy.core.NgModule;
import org.ngoy.q.home.HomeComponent;
import org.ngoy.q.settings.SettingsComponent;
import org.springframework.stereotype.Controller;

@Component(selector = "", templateUrl = "app.component.html")
@NgModule(declarations = { HomeComponent.class, SettingsComponent.class })
@Controller
public class RouterApp {
	public final String appName = "Router";
}
