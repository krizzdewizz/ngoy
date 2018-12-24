package ngoy.core.internal;

import static java.util.Arrays.asList;
import static ngoy.core.Util.escapeHtmlXml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			out.write(escapeHtmlXml(obj.toString()));
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

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> Map(Object... pairs) {
		Map<K, V> map = new HashMap<>();
		for (int i = 0, n = pairs.length; i < n; i += 2) {
			map.put((K) pairs[i], (V) pairs[i + 1]);
		}
		return map;
	}

	@SafeVarargs
	public static <T> List<T> List(T... items) {
		return asList(items);
	}

	@SafeVarargs
	public static <T> Set<T> Set(T... items) {
		return new HashSet<T>(List(items));
	}
}
