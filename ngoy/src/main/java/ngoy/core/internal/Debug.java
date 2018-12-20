package ngoy.core.internal;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Debug {
	private Debug() {
	}

	public static boolean debug() {
		return Boolean.getBoolean("ngoy.debug");
	}

	public static void writeTemplate(String code) {
		if (!debug()) {
//			return;
		}

		try {
			Path tempFile = File.createTempFile("ngoy-template", ".java")
					.toPath();

			tempFile = new File("d:/downloads/qbert.java").toPath();

			Files.write(tempFile, code.getBytes(StandardCharsets.UTF_8));
			System.out.println(format("ngoy.debug: template has been written to %s", tempFile));
		} catch (IOException e) {
			throw wrap(e);
		}
	}
}
