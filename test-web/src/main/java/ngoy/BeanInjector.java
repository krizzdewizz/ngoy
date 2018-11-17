package ngoy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import ngoy.core.Injector;

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