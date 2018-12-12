package ngoy.core.internal;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Util.escape;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ngoy.core.Injector;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;
import ngoy.core.PipeTransform;
import ngoy.core.Provider;

public class Ctx {

	public static Ctx of() {
		return new Ctx();
	}

	public static Ctx of(Injector injector, Map<String, Provider> pipes) {
		return new Ctx(injector, pipes);
	}

	private final Set<Variable> variables = new HashSet<>();
	private final Map<String, Provider> pipeDecls;
	private final Injector injector;
	private PrintStream out;
	private String contentType;

	public static class Variable {
		public final String name;
		public final Object value;

		public Variable(String name, Object value) {
			this.name = name;
			this.value = value;
		}
	}

	private Ctx() {
		this(null, emptyMap());
	}

	private Ctx(@Nullable Injector injector, Map<String, Provider> pipeDecls) {
		this.injector = injector;
		this.pipeDecls = pipeDecls;
	}

	public Ctx variable(String variableName, @Nullable Object variableValue) {
		variables.add(new Variable(variableName, variableValue));
		return this;
	}

	public IterableWithVariables forOfStart(Object iterable) {
		return new IterableWithVariables(evalIterable(iterable));
	}

	@SuppressWarnings({ "rawtypes" })
	private Iterable evalIterable(Object obj) {

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

	public void popCmpContext(Object cmp) {
//		EvaluationContext pop = spelCtxs.pop();
//		Object cmp = pop.getRootObject()
//				.getValue();
		if (cmp instanceof OnDestroy) {
			((OnDestroy) cmp).ngOnDestroy();
		}
	}

	public Object pushCmpContextInput(Class<?> clazz) {
		try {
			Object obj = injector.getNew(clazz);
//			setInputs(clazz, obj, paramPairs);
			return obj;
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public void pushCmpContext(Object cmp) {
		try {
//			Object cmp = injector.get(loadClass(className));

//			// *ngFor on a component
//			Map<String, Object> vars = iterationVars.isEmpty() ? emptyMap() : iterationVars.peek();
//
//			spelCtxs.push(createContext(cmp, vars));

			if (cmp instanceof OnInit) {
				((OnInit) cmp).ngOnInit();
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}

//	public void pushParentContext() {
//		EvaluationContext c = spelCtxs.get(1); // 1 = parent of cmp which
//												// provides ng-content
//		spelCtxs.push(c);
//	}

	public Injector getInjector() {
		return injector;
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

	public Set<Variable> getVariables() {
		return variables;
	}

	public PipeTransform getPipe(String pipeName) {
		Provider pipeProvider = pipeDecls.get(pipeName);
		if (pipeProvider == null) {
			throw new NgoyException("No provider for pipe '%s'", pipeName);
		}
		return (PipeTransform) injector.get(pipeProvider.getProvide());
	}

	public String join(List<String> list, String delimiter) {
		return list.stream()
				.map(Object::toString)
				.collect(joining(delimiter));
	}
}
