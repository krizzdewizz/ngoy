package ngoy.core.reflect;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import ngoy.core.NgoyException;

public abstract class ReflectBinding {

	private interface OnItem {
		void run(String name, Object item, char itemDelimiter, char valueDelimiter);
	}

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

		OnItem onItem = (String name, Object value, char itemDelimiter, char valueDelimiter) -> {
			if (value == null) {
				return;
			}

			String stringValue = value.toString();
			if (stringValue.isEmpty()) {
				return;
			}

			if (itemDelimiter == 0) {
				result.accept(name, stringValue);
				return;
			}

			if (sb.length() > 0) {
				sb.append(itemDelimiter);
			}
			sb.append(name);
			if (valueDelimiter != 0) {
				sb.append(valueDelimiter);
				if (valueDelimiter != 0) {
					sb.append(stringValue);
				}
			}
		};

		if (all != null) {
			for (ReflectBinding binding : all) {
				try {
					Object value = binding.getValue(cmp);
					if (value instanceof Map) {
						@SuppressWarnings("unchecked")
						Map<String, Object> map = (Map<String, Object>) value;
						for (Entry<String, Object> entry : map.entrySet()) {
							onItem.run(entry.getKey(), entry.getValue(), binding.itemDelimiter, binding.valueDelimiter);
						}
						continue;
					}
					onItem.run(binding.name, value, binding.itemDelimiter, binding.valueDelimiter);
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