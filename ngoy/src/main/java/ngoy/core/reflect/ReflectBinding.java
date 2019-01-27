package ngoy.core.reflect;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.function.BiConsumer;

import ngoy.core.NgoyException;

public abstract class ReflectBinding {

	public final String name;
	public final MethodHandle getter;
	private final char itemDelimiter;
	private final char valueDelimiter;

	public ReflectBinding(char itemDelimiter, char valueDelimiter, String name, MethodHandle getter) {
		this.itemDelimiter = itemDelimiter;
		this.valueDelimiter = valueDelimiter;
		this.name = name;
		this.getter = getter;
	}

	protected Object getValue(Object instance) throws Throwable {
		return getter.invoke(instance);
	}

	public static void eval(Object cmp, Collection<? extends ReflectBinding> all, String attrName, Object existingAttrValue, BiConsumer<String, String> result) {
		String existing = existingAttrValue == null ? "" : existingAttrValue.toString();
		if (all == null && existing.isEmpty()) {
			return;
		}
		StringBuilder sb = new StringBuilder(existing);
		if (all != null) {
			for (ReflectBinding binding : all) {
				try {
					char itemDelimiter = binding.itemDelimiter;
					char valueDelimiter = binding.valueDelimiter;
					Object value = binding.getValue(cmp);
					if (value == null || value.toString()
							.isEmpty()) {
						continue;
					}

					if (itemDelimiter == 0) {
						result.accept(binding.name, value.toString());
						continue;
					}

					if (sb.length() > 0) {
						sb.append(itemDelimiter);
					}
					sb.append(binding.name);
					if (valueDelimiter != 0) {
						sb.append(valueDelimiter);
						if (valueDelimiter != 0) {
							sb.append(value);
						}
					}
				} catch (Throwable e) {
					throw new NgoyException(e, "Error while setting component host binding %s.%s: %s", cmp.getClass()
							.getName(), binding.name, e.getMessage());
				}
			}
		}

		if (sb.length() > 0) {
			result.accept(attrName, sb.toString());
		}
	}
}