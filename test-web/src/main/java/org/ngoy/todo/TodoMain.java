package org.ngoy.todo;

import javax.servlet.http.HttpServletResponse;

import org.ngoy.BeanInjector;
import org.ngoy.Ngoy;
import org.ngoy.core.TemplateCache;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

//@Controller
//@RequestMapping("/todo")
public class TodoMain implements InitializingBean {

	private Ngoy ngoy;

	@Autowired
	private BeanInjector beanInjector;

	@GetMapping()
	public void home(HttpServletResponse response) throws Exception {
		ngoy.render(response.getOutputStream());
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		// do not disable in production
		TemplateCache.DEFAULT.setDisabled(true);

		ngoy = Ngoy.app(TodoApp.class)
				.injectors(beanInjector)
				.build();
	}
}