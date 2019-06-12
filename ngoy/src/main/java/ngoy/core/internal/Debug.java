package ngoy.core.internal;

import ngoy.core.NgoyException;
import ngoy.internal.parser.template.CodeBuilder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;

public final class Debug {
    private Debug() {
    }

    private static boolean localDebug = false;

    static {
//		localDebug = true;
    }

    public static boolean debug() {
        if (localDebug) {
            return true;
        }
        return Boolean.getBoolean("ngoy.debug");
    }

    public static void writeTemplate(String className, String code) {

        if (!debug()) {
            return;
        }

        try {
            String pack = "ngoy.core.internal.xtpl";
            String clazz = format("XTemplate_%s", className);

            String fileName = format("%s.java", clazz);

            // without the header, line numbers do not match janino's
            // with the header, the java file can be easier inspected in the ide
            String cu = localDebug //
                    ? new CodeBuilder() {
                @Override
                protected void doCreate() {
                    $("package ", pack, ";");
                    $("public class ", clazz, "{");
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
        } catch (Exception e) {
            new NgoyException(e, "Error while writing ngoy debug file: ", e).printStackTrace();
        }
    }
}
