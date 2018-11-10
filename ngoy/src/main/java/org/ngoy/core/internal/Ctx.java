package org.ngoy.core.internal;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.ngoy.core.NgoyException.wrap;
import static org.ngoy.core.Util.escapeMarkup;
import static org.ngoy.core.Util.findSetter;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ngoy.core.Component;
import org.ngoy.core.Injector;
import org.ngoy.core.NgoyException;
import org.ngoy.core.OnDestroy;
import org.ngoy.core.OnInit;
import org.ngoy.core.PipeTransform;
import org.ngoy.internal.parser.Inputs;
import org.ngoy.internal.util.Nullable;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

public class Ctx {

	public static Ctx of() {
		return new Ctx();
	}

	public static Ctx of(Object modelRoot) {
		return new Ctx(modelRoot, null);
	}

	public static Ctx of(Object modelRoot, Injector injector) {
		return new Ctx(modelRoot, injector);
	}

	private final LinkedList<EvaluationContext> spelCtxs = new LinkedList<>();
	private final Set<String> variables = new HashSet<>();
	private final Object modelRoot;
	private ExpressionParser exprParser;
	private final Injector injector;
	private PrintStream out;
	private String contentType;

	private Ctx() {
		this(null, null);
	}

	private Ctx(@Nullable Object modelRoot, @Nullable Injector injector) {
		this.modelRoot = modelRoot;
		this.injector = injector;
		spelCtxs.push(createContext(modelRoot));
		exprParser = new SpelExpressionParser();
	}

	private EvaluationContext createContext(Object modelRoot) {
		DataBindingPropertyAccessor defaultAccessor = DataBindingPropertyAccessor.forReadOnlyAccess();

		PropertyAccessor accessor = new PropertyAccessor() {

			@Override
			public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
				return true;
			}

			@Override
			public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
				Object val = context.lookupVariable(name);
				if (val != null) {
					return new TypedValue(val);
				}

				if (target == null) {
					throw new NgoyException("Cannot read property '%s' of null", name);
				}

				return defaultAccessor.read(context, target, name);
			}

			@Override
			public Class<?>[] getSpecificTargetClasses() {
				return null;
			}

			@Override
			public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
				return false;
			}

			@Override
			public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
				throw new UnsupportedOperationException();
			}
		};

		SimpleEvaluationContext evalCtx = SimpleEvaluationContext.forPropertyAccessors(accessor)
				.withRootObject(modelRoot)
				.build();
		return new EvalContext(evalCtx);
	}

	public Ctx variable(String variableName, @Nullable Object variableValue) {
		spelCtxs.peek()
				.setVariable(variableName, variableValue);
		variables.add(variableName);
		return this;
	}

	public Object eval(String expr, String[]... pipes) {
		EvaluationContext peek = spelCtxs.peek();
		try {
			Object value = exprParser.parseExpression(expr)
					.getValue(peek);

			for (String[] pipeWithParam : pipes) {
				String pipeClass = pipeWithParam[0];
				List<Object> evaledParams = new ArrayList<>();
				for (int i = 1, n = pipeWithParam.length; i < n; i++) {
					evaledParams.add(eval(pipeWithParam[i]));
				}

				PipeTransform pipe = (PipeTransform) injector.get(loadClass(pipeClass));
				value = pipe.transform(value, evaledParams.toArray());
			}

			return value;
		} catch (Exception e) {
			TypedValue rootObject = peek.getRootObject();
			String root = "null";
			String templateUrl = "none";
			if (rootObject != null) {
				Object value = rootObject.getValue();
				if (value != null) {
					Class<?> rootClass = value.getClass();
					Component cmpAnn = rootClass.getAnnotation(Component.class);
					if (cmpAnn != null) {
						templateUrl = format("'%s'", cmpAnn.templateUrl());
					}

					root = rootClass.getName();
				}
			}
			String msg = format("Error while evaluating expression '%s'. modelRoot: %s. templateUrl: %s, message: %s", expr, root, templateUrl, NgoyException.realException(e));

			throw new NgoyException(msg);
		}
	}

	public boolean evalBool(String expr) {
		return ((Boolean) eval(expr)).booleanValue();
	}

	@SuppressWarnings("rawtypes")
	public Iterable evalIterable(String expr) {
		Object obj = eval(expr);

		if (obj == null) {
			throw new NgoyException("Cannot repeat with a null iterable");
		}

		if (obj instanceof Iterable) {
			return (Iterable) obj;
		}

		if (obj.getClass()
				.isArray()) {
			return new AbstractList() {
				@Override
				public Object get(int index) {
					return Array.get(obj, index);
				}

				@Override
				public int size() {
					return Array.getLength(obj);
				}
			};
		}

		throw new NgoyException("Cannot repeat with an iterable of type %s", obj.getClass()
				.getName());
	}

	public void popCmpContext() {
		EvaluationContext pop = spelCtxs.pop();
		Object cmp = pop.getRootObject()
				.getValue();
		if (cmp instanceof OnDestroy) {
			((OnDestroy) cmp).ngOnDestroy();
		}
	}

	public void popContext() {
		spelCtxs.pop();
	}

	public void pushCmpContext(String className, String... paramPairs) {
		try {
			Class<?> clazz = loadClass(className);
			Object cmp = injector.get(clazz);

			for (int i = 0, n = paramPairs.length; i < n; i += 2) {
				String input = paramPairs[i];
				char inputType = input.charAt(0);
				input = input.substring(1);
				String expr = paramPairs[i + 1];

				Object value = eval(expr);
				switch (inputType) {
				case Inputs.FIELD_INPUT: {
					Field field = clazz.getField(input);
					try {
						field.set(cmp, value);
					} catch (Exception e) {
						String fieldType = field.getType()
								.getName();
						Object valueType = value == null ? null
								: value.getClass()
										.getName();
						throw new NgoyException("Error while setting input field %s.%s to result of expression '%s'. Field type: %s, expression result type: %s", className, field.getName(), expr,
								fieldType, valueType);
					}
				}
					break;
				case Inputs.METHOD_INPUT: {
					Method setter = findSetter(clazz, input);
					try {
						setter.invoke(cmp, value);
					} catch (Exception e) {
						String parameterType = setter.getParameterTypes()[0].getName();
						Object valueType = value == null ? null
								: value.getClass()
										.getName();
						throw new NgoyException("Error while invoking input setter %s.%s with result of expression '%s'. Parameter type: %s, expression result type: %s", className, setter.getName(),
								expr, parameterType, valueType);
					}
				}
					break;
				default:
					throw new NgoyException("Unknown input type: %s", inputType);
				}
			}

			spelCtxs.push(createContext(cmp));

			if (cmp instanceof OnInit) {
				((OnInit) cmp).ngOnInit();
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public void pushContext(String variableName, Object variableValue) {
		EvaluationContext tmpCtx = createContext(modelRoot);
		EvaluationContext parentCtx = spelCtxs.peek();
		variables.forEach(name -> tmpCtx.setVariable(name, parentCtx.lookupVariable(name)));
		tmpCtx.setVariable(variableName, variableValue);
		spelCtxs.push(tmpCtx);
	}

	public void pushParentContext() {
		EvaluationContext c = spelCtxs.get(1); // 1 = parent of cmp which
												// provides ng-content
		spelCtxs.push(c);
	}

	private Class<?> loadClass(String pipeClass) throws ClassNotFoundException {
		return Thread.currentThread()
				.getContextClassLoader()
				.loadClass(pipeClass);
	}

	public Injector getInjector() {
		return injector;
	}

	/**
	 * @return null if none of the classes shall be added
	 */
	@Nullable
	public String evalClasses(String[]... classExprPairs) {
		List<String> classList = new ArrayList<>();
		for (String[] pair : classExprPairs) {
			String clazz = pair[0];
			String expr = pair[1];
			if (expr.isEmpty() || evalBool(expr)) {
				classList.add(clazz);
			}
		}
		return classList.isEmpty() ? null
				: classList.stream()
						.collect(joining(" "));
	}

	public void printEscaped(@Nullable Object obj) {
		if (obj != null) {
			print(escapeMarkup(obj.toString(), contentType));
		}
	}

	public void print(@Nullable Object obj) {
		if (obj != null) {
			out.print(obj);
		}
	}

	public void setOut(PrintStream out, String contentType) {
		this.out = out;
		this.contentType = contentType;
	}

	public void resetOut() {
		this.out = null;
		this.contentType = null;
	}
}
