package org.ngoy;

import org.ngoy.core.Injector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class BeanInjector implements Injector {

	@Autowired
	private ApplicationContext context;

	@Override
	public <T> T get(Class<T> clazz) {
		return context.getBeansOfType(clazz)
				.isEmpty() ? null : context.getBean(clazz);
	}

}