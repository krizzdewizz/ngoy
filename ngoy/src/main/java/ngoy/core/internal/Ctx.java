package ngoy.core.internal;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static ngoy.core.Util.escape;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import ngoy.core.Injector;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;
import ngoy.core.PipeTransform;
import ngoy.core.Provider;
import ngoy.core.Variable;

public class Ctx {

	public static Ctx of() {
		return new Ctx();
	}

	public static Ctx of(Injector injector, Map<String, Provider> pipes) {
		return new Ctx(injector, pipes);
	}

	private final Map<String, Provider> pipeDecls;
	private final Injector injector;
	private PrintStream out;
	private String contentType;
	private Map<String, Variable<?>> variables = new HashMap<>();

	private Ctx() {
		this(null, emptyMap());
	}

	private Ctx(@Nullable Injector injector, Map<String, Provider> pipeDecls) {
		this.injector = injector;
		this.pipeDecls = pipeDecls;
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

	public void cmpDestroy(Object cmp) {
		if (cmp instanceof OnDestroy) {
			((OnDestroy) cmp).ngOnDestroy();
		}
	}

	public Object cmpNew(Class<?> clazz) {
		return injector.getNew(clazz);
	}

	public Object cmp(Class<?> clazz) {
		return injector.get(clazz);
	}

	public void cmpInit(Object cmp) {
		if (cmp instanceof OnInit) {
			((OnInit) cmp).ngOnInit();
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

	public Map<String, Variable<?>> getVariables() {
		return variables;
	}

	public Object getVariableValue(String name) {
		Variable<?> variable = getVariables().get(name);
		if (variable == null) {
			throw new NgoyException("Variable '%s' could not be found", name);
		}
		return variable.value;
	}

	public void setVariables(Map<String, Variable<?>> variables) {
		this.variables = variables;
	}

	public PipeTransform getPipe(String pipeName) {
		Provider pipeProvider = pipeDecls.get(pipeName);
		if (pipeProvider == null) {
			throw new NgoyException("No provider for pipe '%s'", pipeName);
		}
		return (PipeTransform) injector.get(pipeProvider.getProvide());
	}

	public static String join(Collection<String> list, String delimiter) {
		StringJoiner joiner = new StringJoiner(delimiter);
		for (String it : list) {
			joiner.add(it);
		}
		return joiner.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map Map(Object... pairs) {
		Map map = new HashMap();
		for (int i = 0, n = pairs.length; i < n; i += 2) {
			map.put(pairs[i], pairs[i + 1]);
		}
		return map;
	}

	@SuppressWarnings({ "rawtypes" })
	public static List List(Object... items) {
		return asList(items);
	}
}
