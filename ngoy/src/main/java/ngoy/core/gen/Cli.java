package ngoy.core.gen;

import static java.lang.String.format;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import ngoy.Version;
import ngoy.core.NgoyException;
import ngoy.core.Util;

public class Cli {

	private static final Options options;
	static {
		options = new Options() //
				.addOption("h", "help", false, "display this help")
				.addOption("p", "package", true, "package prefix for the generated artifact. Default is 'ngoygen'.")
				.addOption("t", "target", true, "target folder for the generated artifacts. A default is searched in the following order: [./src/main/java, ./src, .]")
				.addOption(null, "version", false, "print version information");
	}

	private final Generator generator = new Generator();

	public void run(String[] args, OutputStream out) {
		CommandLine cmd;
		try {
			CommandLineParser parser = new DefaultParser();
			cmd = parser.parse(options, args);

			if (cmd.hasOption("version")) {
				printVersion();
				return;
			}

			PrintStream psOut = Util.newPrintStream(out);
			generator.setLog(psOut::println);

			List<String> argList = cmd.getArgList();

			String targetOption = cmd.getOptionValue('t');
			Path target = targetOption != null ? getCwd().resolve(targetOption) : findSrcFolder();

			if (cmd.hasOption('h') || !generate(argList, cmd.getOptionValue('p'), target)) {
				printHelp();
				return;
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			generator.setLog(null);
		}
	}

	private boolean generate(List<String> argList, String pack, Path target) {
		if (argList.size() < 2) {
			return false;
		}

		if (pack == null || pack.isEmpty()) {
			pack = "ngoygen";
		}

		char kind = argList.get(0)
				.charAt(0);
		String name = argList.get(1);

		if (kind == 'c') {
			pack = format("%s.%s", pack, name.replace('-', '_'));
		}

		GenModel model = new GenModel(pack, name);

		switch (kind) {
		case 'c':
			generator.component(model, target);
			return true;
		case 'd':
			generator.directive(model, target);
			return true;
		case 'p':
			generator.pipe(model, target);
			return true;
		case 'm':
			generator.mod(model, target);
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
		new HelpFormatter().printHelp("ngoy-gen [options] component|directive|pipe|module name",
				"\nname should be lower-case-separated-with-dashes.\n\nExamples:\n  ngoy-gen component person-list\n  ngoy-gen -p com.example pipe quantity-format\n\nShortcuts works as well:\n  ngoy-gen p my-pipe\n\nOptions:",
				options, "");
	}

	private void printVersion() {
		System.out.println(Version.getImplementationVersion());
	}

	public static void main(String[] args) {
		new Cli().run(args, System.out);
	}
}
