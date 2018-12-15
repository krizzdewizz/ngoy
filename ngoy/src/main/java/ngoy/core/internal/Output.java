package ngoy.core.internal;

public interface Output extends AutoCloseable {
	void write(String string);
}
