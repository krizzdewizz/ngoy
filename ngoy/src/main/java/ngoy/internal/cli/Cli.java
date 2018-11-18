package ngoy.internal.cli;

import static java.lang.String.format;
import static ngoy.Ngoy.renderString;
import static ngoy.core.NgoyException.wrap;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ngoy.Version;
import ngoy.core.Context;

public class Cli {

	private static final Options options;
	static {
		options = new Options() //
				.addOption("h", "help", false, "display this help")
				.addOption("e", "expression", false, "treat template as an expression")
				.addOption("f", "file", false, "read template from file instead of command line")
				.addOption(null, "version", false, "print version information")
				.addOption(Option.builder("v")
						.longOpt("variable")
						.argName("name=value")
						.desc("add a variable to the execution context")
						.numberOfArgs(2)
						.valueSeparator()
						.build());
	}

	public void run(String[] args, OutputStream out) {

		CommandLine cmd;
		try {
			CommandLineParser parser = new DefaultParser();
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			return;
		}

		if (cmd.hasOption("version")) {
			printVersion();
			return;
		}

		List<String> argList = cmd.getArgList();

		if (argList.isEmpty() || cmd.hasOption('h')) {
			printHelp();
			return;
		}

		Context context = Context.of(new Global());
		String[] vars = cmd.getOptionValues('v');
		if (vars != null) {
			for (int i = 0, n = vars.length; i < n; i += 2) {
				context.variable(vars[i], vars[i + 1]);
			}
		}

		boolean expr = cmd.hasOption('e');
		String template = readTemplate(argList.get(0), cmd.hasOption('f'));
		String tpl = expr ? format("{{ %s }}", template) : template;

		renderString(tpl, context, out);
	}

	private void printHelp() {
		new HelpFormatter().printHelp("ngoy [options] template", "\nOptions", options, "");
	}

	private void printVersion() {
		System.out.println(Version.getImplementationVersion());
	}

	private String readTemplate(String template, boolean isFile) {
		if (isFile) {
			try {
				return new String(Files.readAllBytes(Paths.get(template)), "UTF-8");
			} catch (Exception e) {
				throw wrap(e);
			}
		}

		return template;
	}
}
