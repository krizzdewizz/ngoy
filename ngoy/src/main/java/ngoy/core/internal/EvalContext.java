package ngoy.core.internal;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;

import org.springframework.expression.BeanResolver;
import org.springframework.expression.ConstructorResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.OperatorOverloader;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypeComparator;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.ReflectiveConstructorResolver;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;
import org.springframework.expression.spel.support.StandardTypeLocator;

public class EvalContext implements EvaluationContext {

	private final List<ConstructorResolver> constructorResolvers;
	private final EvaluationContext target;
	private final TypeLocator typeLocator;
	private final List<MethodResolver> methodResolvers;
	private final Map<String, Object> variables;

	public EvalContext(EvaluationContext target, Map<String, Object> variables) {
		this.target = target;
		this.variables = variables;
		constructorResolvers = asList(new ReflectiveConstructorResolver());
		methodResolvers = asList(new ReflectiveMethodResolver());

		StandardTypeLocator locator = new StandardTypeLocator();
		locator.registerImport("java.util");
		locator.registerImport("java.time");
		locator.registerImport("java.math");
		typeLocator = locator;
	}

	public List<ConstructorResolver> getConstructorResolvers() {
		return constructorResolvers;
	}

	public List<MethodResolver> getMethodResolvers() {
		return methodResolvers;
	}

	public TypeLocator getTypeLocator() {
		return typeLocator;
	}

	public Object lookupVariable(String name) {
		Object val = variables.get(name);
		return val != null ? val : target.lookupVariable(name);
	}

	///////////

	public TypedValue getRootObject() {
		return target.getRootObject();
	}

	public List<PropertyAccessor> getPropertyAccessors() {
		return target.getPropertyAccessors();
	}

	public BeanResolver getBeanResolver() {
		return target.getBeanResolver();
	}

	public TypeConverter getTypeConverter() {
		return target.getTypeConverter();
	}

	public TypeComparator getTypeComparator() {
		return target.getTypeComparator();
	}

	public OperatorOverloader getOperatorOverloader() {
		return target.getOperatorOverloader();
	}

	public void setVariable(String name, Object value) {
		target.setVariable(name, value);
	}
}
