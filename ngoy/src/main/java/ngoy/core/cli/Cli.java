package ngoy.core.cli;

import static java.util.Arrays.asList;
import static ngoy.Ngoy.renderString;
import static ngoy.core.NgoyException.wrap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ngoy.Ngoy.Config;
import ngoy.Version;
import ngoy.core.Context;

public class Cli {

	private static final Options options;
	static {
		options = new Options() //
				.addOption("h", "help", false, "display this help")
				.addOption("e", "expression", false, "treat template as an expression")
				.addOption("f", "file", false, "read template from file instead of command line")
				.addOption("in", "input", false, "run template for each line read from stdin (use $ variable to access line within template)")
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

		if (isGen(args)) {
			List<String> rest = asList(args).subList(1, args.length);
			createGenCli().run(rest.toArray(new String[rest.size()]), out);
			return;
		}

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
		Config config = new Config();
		config.templateIsExpression = expr;
		if (cmd.hasOption("in")) {
			eachLine(System.in, line -> {
				context.variable("$", line);
				renderString(template, context, out, config);
			});
		} else {
			renderString(template, context, out, config);
		}
	}

	private boolean isGen(String[] args) {
		return args.length > 0 && (args[0].equals("g") || args[0].equals("gen") || args[0].equals("generate"));
	}

	protected ngoy.core.gen.Cli createGenCli() {
		return new ngoy.core.gen.Cli();
	}

	private void printHelp() {
		new HelpFormatter().printHelp("ngoy [g|gen|generate] [options] template", "\nIf generate is given, the rest of the arguments are passed over to ngoy-gen.\n\nOptions:", options, "");
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

	void eachLine(InputStream in, Consumer<String> lineConsumer) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				lineConsumer.accept(line);
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}
