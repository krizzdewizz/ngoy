package ngoy.core.internal;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ngoy.internal.parser.template.CodeBuilder;

public final class Debug {
	private Debug() {
	}

	public static boolean debug() {
		return Boolean.getBoolean("ngoy.debug");
	}

	private static int templateIndex = 0;

	public static void writeTemplate(String code) {

		boolean localDebug = false;
		boolean incrTemplateIndex = false;
		localDebug = true;
//		incrTemplateIndex = true;

		if (!debug() && !localDebug) {
			return;
		}

		try {
			String pack = "ngoy.core.internal";
			String clazz = "XTemplate";

			if (incrTemplateIndex) {
				clazz += (templateIndex++);
			}

			String clazzz = clazz;

			String fileName = format("%s.java", clazz);

			// without the header, line numbers do not match janino's
			// with the header, the java file can be easier inspected in the ide
			String cu = localDebug //
					? new CodeBuilder() {
						@Override
						protected void doCreate() {
							$("package ", pack, ";");
							$("public class ", clazzz, "{");
							$$(code);
							$("}");
						}
					}.create()
							.toString()
					: code;

			Path tempFile;
			if (localDebug) {
				tempFile = Paths.get(System.getProperty("user.dir"), "src/test/java/".concat(pack.replace('.', '/')), fileName);
			} else {
				tempFile = File.createTempFile("ngoy-template-", ".java")
						.toPath();
			}

			Files.createDirectories(tempFile.getParent());

			Files.write(tempFile, cu.getBytes(StandardCharsets.UTF_8));
			System.out.println(format("ngoy.debug: template has been written to %s", tempFile));
		} catch (IOException e) {
			throw wrap(e);
		}
	}
}
