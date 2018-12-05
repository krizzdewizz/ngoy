package ngoy.core.internal;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Util.escape;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import ngoy.core.Component;
import ngoy.core.Injector;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;
import ngoy.core.PipeTransform;
import ngoy.core.Provider;
import ngoy.core.internal.IterableWithVariables.IterVariable;
import ngoy.internal.parser.ForOfVariable;
import ngoy.internal.parser.Inputs;

public class Ctx {

	public static Ctx of() {
		return new Ctx();
	}

	public static Ctx of(Object model) {
		return new Ctx(model, null, emptyMap());
	}

	public static Ctx of(Object model, Injector injector, Map<String, Provider> pipes) {
		return new Ctx(model, injector, pipes);
	}

	public static boolean eq(Object a, Object b) {
		return Objects.equals(a, b);
	}

	public class PipeMethodResolver implements MethodResolver {
		@Override
		public MethodExecutor resolve(EvaluationContext context, Object targetObject, String name, List<TypeDescriptor> argumentTypes) throws AccessException {
			if (!name.startsWith("$")) {
				return null;
			}

			if (argumentTypes.isEmpty()) {
				throw new NgoyException("Missing first argument to invocation of pipe '%s'", name);
			}

			String pipeName = name.substring(1);
			Provider pipeProvider = pipeDecls.get(pipeName);
			if (pipeProvider == null) {
				throw new NgoyException("No provider for pipe '%s'", pipeName);
			}
			PipeTransform pipe = (PipeTransform) injector.get(pipeProvider.getProvide());
			return (EvaluationContext ctx, Object target, Object... arguments) -> {
				List<Object> rest = asList(arguments).subList(1, arguments.length);
				return new TypedValue(pipe.transform(arguments[0], rest.toArray(new Object[rest.size()])));
			};
		}
	}

	private final LinkedList<EvaluationContext> spelCtxs = new LinkedList<>();
	private final Set<String> variables = new HashSet<>();
	private final ExpressionParser exprParser = new SpelExpressionParser();
	private final LinkedList<Map<String, Object>> iterationVars = new LinkedList<>();
	private final ExprCache exprCache = new ExprCache();
	private final PipeMethodResolver pipeMethodResolver = new PipeMethodResolver();
	private final Map<String, Provider> pipeDecls;
	private final Injector injector;
	private PrintStream out;
	private String contentType;

	private Ctx() {
		this(null, null, emptyMap());
	}

	private Ctx(@Nullable Object model, @Nullable Injector injector, Map<String, Provider> pipeDecls) {
		this.injector = injector;
		this.pipeDecls = pipeDecls;
		spelCtxs.push(createContext(model, emptyMap()));
	}

	private EvalContext createContext(Object model, Map<String, Object> variables) {
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
				.withRootObject(model)
				.build();
		return new EvalContext(evalCtx, variables, pipeMethodResolver);
	}

	public Ctx variable(String variableName, @Nullable Object variableValue) {
		spelCtxs.peek()
				.setVariable(variableName, variableValue);
		variables.add(variableName);
		return this;
	}

	public Object eval(String expr) {
		EvaluationContext peek = spelCtxs.peek();
		try {
			Object value = exprCache.get(expr, exprParser)
					.getValue(peek);
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
			throw new NgoyException(format("Error while evaluating expression '%s'. model: %s. templateUrl: %s, message: %s.", expr, root, templateUrl, NgoyException.realException(e)));
		}
	}

	public boolean evalBool(String expr) {
		try {
			Object obj = eval(expr);
			Boolean result = (Boolean) obj;
			if (result == null) {
				throw new NgoyException("result is null");
			}
			return result.booleanValue();
		} catch (Exception e) {
			TypedValue rootObject = spelCtxs.peek()
					.getRootObject();
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
			throw new NgoyException(e, format("Error while evaluating boolean expression '%s': %s. model: %s. templateUrl: %s.", expr, e.getMessage(), root, templateUrl));
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public Iterable forOfStart(String expr, String[] variables) {
		Map<String, Object> vars = new HashMap<>();
		IterVariable iterVars = (variable, value) -> vars.put(variable, value);
		iterationVars.push(vars);

		return new IterableWithVariables(evalIterable(expr), ForOfVariable.valueOf(variables), iterVars);
	}

	@SuppressWarnings({ "rawtypes" })
	private Iterable evalIterable(String expr) {
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

	public void forOfEnd() {
		iterationVars.pop();
	}

	public void popContext() {
		spelCtxs.pop();
	}

	public void pushCmpContextInput(String className, String... paramPairs) {
		try {
			setInputs(loadClass(className), injector.getNew(loadClass(className)), paramPairs);
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public void pushCmpContext(String className) {
		try {
			Object cmp = injector.get(loadClass(className));

			// *ngFor on a component
			Map<String, Object> vars = iterationVars.isEmpty() ? emptyMap() : iterationVars.peek();

			spelCtxs.push(createContext(cmp, vars));

			if (cmp instanceof OnInit) {
				((OnInit) cmp).ngOnInit();
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private void setInputs(Class<?> clazz, Object cmp, String... paramPairs) throws NoSuchFieldException {
		for (int i = 0, n = paramPairs.length; i < n; i += 2) {
			String input = paramPairs[i];
			char inputType = input.charAt(0);
			char inputValueType = input.charAt(1);
			input = input.substring(2);
			String expr = paramPairs[i + 1];

			Object value = inputValueType == Inputs.VALUE_EXPR ? eval(expr) : expr;
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
					throw new NgoyException(e, "Error while setting input field %s.%s to result of expression '%s'. Field type: %s, expression result type: %s", clazz.getName(), field.getName(), expr,
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
					throw new NgoyException(e, "Error while invoking input setter %s.%s with result of expression '%s'. Parameter type: %s, expression result type: %s", clazz.getName(),
							setter.getName(), expr, parameterType, valueType);
				}
			}
				break;
			default:
				throw new NgoyException("Unknown input type: %s", inputType);
			}
		}
	}

	public void pushForOfContext(String itemName, Object item) {
		EvaluationContext parentCtx = spelCtxs.peek();
		EvaluationContext iterCtx = createContext(parentCtx.getRootObject()
				.getValue(), emptyMap());
		if (parentCtx != null) {
			variables.forEach(name -> {
				iterCtx.setVariable(name, parentCtx.lookupVariable(name));
			});
		}

		Map<String, Object> iterVars = iterationVars.peek();
		for (Map.Entry<String, Object> e : iterVars.entrySet()) {
			iterCtx.setVariable(e.getKey(), e.getValue());
		}

		iterCtx.setVariable(itemName, item);
		spelCtxs.push(iterCtx);
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

			if (clazz.equals("ngClass")) {
				// Spring EL evals an object literal such as {a: true} properly
				// to a map
				@SuppressWarnings("unchecked")
				Map<String, Boolean> map = (Map<String, Boolean>) eval(expr);
				for (Map.Entry<String, Boolean> entry : map.entrySet()) {
					if (entry.getValue()) {
						classList.add(entry.getKey());
					}
				}
			} else {
				if (expr.isEmpty() || evalBool(expr)) {
					classList.add(clazz);
				}
			}
		}

		return classList.isEmpty() ? null
				: classList.stream()
						.collect(joining(" "));
	}

	/**
	 * @return null if none of the classes shall be added
	 */
	@Nullable
	public String evalStyles(String[]... styleExprPairs) {
		List<String> styleList = new ArrayList<>();

		for (String[] pair : styleExprPairs) {
			String style = pair[0];
			String expr = pair[1];

			if (style.equals("ngStyle")) {
				// Spring EL evals an object literal such as {a: 'hello'}
				// properly to a map
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) eval(expr);
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String st = entry.getKey();
					Object v = entry.getValue();

					if (isNullOrEmptyString(v)) {
						continue;
					}
					styleList.add(format("%s:%s", parseUnit(st, v)));
				}
			} else {
				if (expr.isEmpty()) {
					styleList.add(style);
				} else {
					Object v = eval(expr);
					if (!isNullOrEmptyString(v)) {
						styleList.add(format("%s:%s", parseUnit(style, v)));
					}
				}
			}
		}

		return styleList.isEmpty() ? null
				: styleList.stream()
						.collect(joining(";"));
	}

	private boolean isNullOrEmptyString(Object v) {
		return v == null || (v instanceof String && ((String) v).isEmpty());
	}

	private Object[] parseUnit(String s, Object val) {
		int pos = s.lastIndexOf('.');
		if (pos < 0) {
			return new Object[] { s, val };
		}

		String style = s.substring(0, pos);
		String unit = s.substring(pos + 1);
		return new Object[] { style, val + unit };
	}

	public void printEscaped(@Nullable Object obj) {
		if (obj != null) {
			print(escape(obj.toString(), contentType));
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

	static Method findSetter(Class<?> clazz, String name) {
		return Stream.of(clazz.getMethods())
				.filter(m -> m.getName()
						.equals(name) && m.getParameterCount() == 1)
				.findFirst()
				.orElseThrow(() -> new NgoyException("Setter method %s%s could not be found or has not exactly 1 parameter", clazz.getName(), name));
	}

	public Set<String> getVariables() {
		return variables;
	}
}
