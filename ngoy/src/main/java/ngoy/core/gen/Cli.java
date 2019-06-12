package ngoy.core.gen;

import ngoy.Version;
import ngoy.core.NgoyException;
import ngoy.core.cli.internal.Formatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import static ngoy.core.NgoyException.wrap;

public class Cli {

    private static final Options options;

    static {
        options = new Options()
                .addOption("h", "help", false, "display this help")
                .addOption("t", "target", true, "target folder for the generated artifacts. A default is searched in the following order: [./src/main/java, ./src, .]")
                .addOption(null, "version", false, "print version information");
    }

    private final Generator generator = new Generator();
    private String appPrefix;

    private void loadProperties() {

        appPrefix = "app";

        Path propsPath = getPropertiesPath();
        if (!Files.exists(propsPath)) {
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(propsPath)) {
            props.load(in);

            ifPropSet(props, "app.prefix", prop -> appPrefix = prop);
        } catch (IOException e) {
            throw wrap(e);
        }
    }

    protected Path getPropertiesPath() {
        return Paths.get("ngoy.properties");
    }

    public void run(String[] args, Writer out) {
        try {
            doRun(args, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRun(String[] args, Writer out) {

        loadProperties();

        CommandLine cmd;
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);

            if (cmd.hasOption("version")) {
                printVersion();
                return;
            }

            generator.setLog(s -> {
                try {
                    out.write(s);
                    out.write(System.lineSeparator());
                    out.flush();
                } catch (IOException e) {
                    throw NgoyException.wrap(e);
                }
            });

            List<String> argList = cmd.getArgList();

            String targetOption = cmd.getOptionValue('t');
            Path target = targetOption != null ? getCwd().resolve(targetOption) : findSrcFolder();

            if (cmd.hasOption('h') || !generate(argList, target)) {
                printHelp();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            generator.setLog(null);
        }
    }

    private boolean generate(List<String> argList, Path target) {
        if (argList.size() < 2) {
            return false;
        }

        String artifactKind = argList.get(0);

        String kind;
        if (artifactKind.length() == 1) {
            if (artifactKind.charAt(0) == 'p') {
                throw new NgoyException("Ambiguous artifact type: %s. Could be [p]roject] or [p]ipe.", artifactKind);
            }
            kind = artifactKind;
        } else {
            kind = artifactKind.substring(0, 2);
        }

        String name = argList.get(1);

        GenModel model = new GenModel(appPrefix, name, Version.getVersion());

        switch (kind) {
            case "c":
                generator.component(model, target);
                return true;
            case "d":
                generator.directive(model, target);
                return true;
            case "m":
                generator.mod(model, target);
                return true;
            case "s":
                generator.service(model, target);
                return true;
            case "pi":
                generator.pipe(model, target);
                return true;
            case "pr":
                generator.project(model, target);
                return true;
            default:
                throw new NgoyException("Unknown artifact type '%s'", kind);
        }
    }

    private Path findSrcFolder() {
        Path cwd = getCwd();
        Path src = cwd.resolve("src");
        if (!Files.exists(src)) {
            return cwd;
        }
        Path java = src.resolve("main/java");
        return Files.exists(java) ? java : src;
    }

    protected Path getCwd() {
        return Paths.get(".");
    }

    private void printHelp() {
        new Formatter().printHelp("ngoy-gen [options] project|component|directive|pipe|module|service name",
                "\n'name' should be a fully qualified Java class name.\n\nExamples:\n  ngoy-gen component org.myapp.person.PersonList\n\nShortcuts works as well:\n  ngoy-gen pi org.myapp.MyPipe\n\nOptions:",
                options, "");
    }

    private void printVersion() {
        System.out.println(Version.getImplementationVersion());
    }

    public static void main(String[] args) throws Exception {
        OutputStreamWriter out = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
        try {
            new Cli().run(args, out);
        } finally {
            out.flush();
        }
    }

    private void ifPropSet(Properties props, String name, Consumer<String> value) {
        String prop = props.getProperty(name);
        if (prop != null) {
            value.accept(prop.trim());
        }
    }
}
