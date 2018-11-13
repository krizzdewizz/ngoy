package org.ngoy.core.internal;

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

	private final List<ConstructorResolver> ctorResolvers;
	private final EvaluationContext target;
	private final StandardTypeLocator typeLocator;
	private final List<MethodResolver> methodResolvers;
	private final Map<String, Object> variables;

	public EvalContext(EvaluationContext target, Map<String, Object> variables) {
		this.target = target;
		this.variables = variables;
		ctorResolvers = asList(new ReflectiveConstructorResolver());
		typeLocator = new StandardTypeLocator();
		methodResolvers = asList(new ReflectiveMethodResolver());
	}

	public List<ConstructorResolver> getConstructorResolvers() {
		return ctorResolvers;
	}

	public List<MethodResolver> getMethodResolvers() {
		return methodResolvers;
	}

	public TypeLocator getTypeLocator() {
		return typeLocator;
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

	public Object lookupVariable(String name) {
		Object val = variables.get(name);
		return val != null ? val : target.lookupVariable(name);
	}

}
