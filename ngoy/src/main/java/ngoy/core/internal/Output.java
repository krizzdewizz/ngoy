package ngoy.core.internal;

public interface Output extends AutoCloseable {
	void write(byte[] bytes);

	void write(String string);
}
