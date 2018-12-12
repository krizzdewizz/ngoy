package ngoy.core;

public class Variable<T> {
	public final Class<T> type;
	public final T value;

	public Variable(Class<T> type, T value) {
		this.type = type;
		this.value = value;
	}
}
