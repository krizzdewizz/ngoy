package org.ngoy;

import static org.ngoy.core.Provider.useValue;

import org.ngoy.core.TemplateCache;
import org.ngoy.todo.AppComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TodoViewController implements InitializingBean {

	@SuppressWarnings("unused")
	private Ngoy ngoy;

	@Autowired
	private BeanInjector beanInjector;

	@Autowired
	private AppComponent app;

	@Override
	public void afterPropertiesSet() throws Exception {

		// do not disable in production
		TemplateCache.DEFAULT.setDisabled(true);

		ngoy = Ngoy.app(AppComponent.class)
				.providers(useValue(AppComponent.class, app))
				.injectors(beanInjector)
				.build();
	}
}