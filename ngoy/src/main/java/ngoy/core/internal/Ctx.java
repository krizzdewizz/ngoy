package ngoy.core.internal;

import static java.util.Arrays.asList;
import static ngoy.core.Util.escape;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ngoy.core.Injector;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;
import ngoy.core.Variable;

public class Ctx {

	public static Ctx of() {
		return new Ctx();
	}

	public static Ctx of(Injector injector) {
		return new Ctx(injector);
	}

	private final Injector injector;
	private Output out;
	private Map<String, Variable<?>> variables = new HashMap<>();

	private Ctx() {
		this(null);
	}

	private Ctx(@Nullable Injector injector) {
		this.injector = injector;
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

	public void pe(@Nullable Object obj) {
		if (obj != null) {
			out.write(escape(obj.toString()));
		}
	}

	public void p(@Nullable Object obj) {
		if (obj != null) {
			out.write(obj.toString());
		}
	}

	public void setOut(Output out) {
		this.out = out;
	}

	public void resetOut() {
		this.out = null;
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
