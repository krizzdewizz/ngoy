package ngoy.core.internal;

import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Util.escapeHtmlXml;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import ngoy.core.Injector;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.Output;
import ngoy.core.Variable;

public class Ctx implements Output {

	private final Object cmpInstance;
	private final Injector injector;
	private final Writer writer;
	private Map<String, Variable<?>> variables = new HashMap<>();

	public Ctx(Object cmpInstance, Injector injector, Writer out) {
		this.cmpInstance = cmpInstance;
		this.injector = injector;
		this.writer = out;
	}

	public Object cmpNew(Class<?> clazz) {
		return injector.getNew(clazz);
	}

	public Object cmp(Class<?> clazz) {
		return injector.get(clazz);
	}

	public void pe(@Nullable Object obj) {
		if (obj != null) {
			writeEscaped(obj.toString());
		}
	}

	public void p(@Nullable Object obj) {
		if (obj != null) {
			write(obj.toString());
		}
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

	public void write(String string) {
		try {
			writer.write(string);
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public void writeEscaped(String string) {
		write(escapeHtmlXml(string));
	}

	@Override
	public Writer getWriter() {
		return writer;
	}

	public Object getCmpInstance() {
		return cmpInstance;
	}
}
