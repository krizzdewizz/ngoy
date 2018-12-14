package ngoy.core.internal;

import static ngoy.core.NgoyException.wrap;

import java.io.IOException;
import java.io.OutputStream;

public class StreamOutput implements Output {

	private final OutputStream out;

	public StreamOutput(OutputStream out) {
		this.out = out;
	}

	@Override
	public void write(byte[] bytes) {
		try {
			out.write(bytes);
		} catch (IOException e) {
			throw wrap(e);
		}
	}

	@Override
	public void write(String string) {
		try {
			write(string.getBytes("UTF-8"));
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public void flush() {
		try {
			out.flush();
		} catch (IOException e) {
			throw wrap(e);
		}
	}

	@Override
	public void close() throws Exception {
		out.close();
	}
}
