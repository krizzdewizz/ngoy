package ngoy.core.gen;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import ngoy.Ngoy;
import ngoy.Ngoy.Config;
import ngoy.core.Context;
import ngoy.core.NgoyException;

public class Generator {

	private static final Consumer<String> NIRVANA = s -> {
	};

	private Consumer<String> log = NIRVANA;

	public void directive(GenModel genModel, Path targetFolder) {
		generateArtifacts(genModel, targetFolder, "directive", "$nameDirective.java.tpl");
	}

	public void mod(GenModel genModel, Path targetFolder) {
		generateArtifacts(genModel, targetFolder, "mod", "$nameModule.java.tpl");
	}

	public void pipe(GenModel genModel, Path targetFolder) {
		generateArtifacts(genModel, targetFolder, "pipe", "$namePipe.java.tpl");
	}

	public void component(GenModel genModel, Path targetFolder) {
		generateArtifacts(genModel, targetFolder, "component", "$nameComponent.java.tpl", "$name.component.html.tpl", "$name.component.css.tpl");
	}

	public void service(GenModel genModel, Path targetFolder) {
		generateArtifacts(genModel, targetFolder, "service", "$nameService.java.tpl");
	}

	public void project(GenModel genModel, Path targetFolder) {

		if (existsAndNotEmpty(targetFolder)) {
			throw new NgoyException("Target folder must be empty: %s", targetFolder);
		}

		String[] all = { //
				".gitignore.tpl", //
				"build.gradle.tpl", //
				"ngoy.cmd.tpl", //
				"ngoy.tpl", //
				"settings.gradle.tpl", //
				"src/main/java/$pack/$nameWebApplication.java.tpl", //
				"src/main/java/$pack/app/app.component.css.tpl", //
				"src/main/java/$pack/app/app.component.html.tpl", //
				"src/main/java/$pack/app/AppComponent.java.tpl", //
				"src/main/java/$pack/app/Main.java.tpl", //
				"src/main/resources/application.properties.tpl", //
				"src/main/resources/messages_en.properties.tpl", //
		};
		generateArtifacts(genModel, "", targetFolder, "project", all);
		initGit(targetFolder);
	}

	private boolean existsAndNotEmpty(Path targetFolder) {
		if (Files.notExists(targetFolder)) {
			return false;
		}
		try {
			return Files.list(targetFolder)
					.findFirst()
					.isPresent();
		} catch (IOException e) {
			throw wrap(e);
		}
	}

	private void generateArtifacts(GenModel genModel, Path targetFolder, String tplRoot, String... tpls) {
		generateArtifacts(genModel, genModel.pack.replace('.', '/'), targetFolder, tplRoot, tpls);
	}

	private void generateArtifacts(GenModel genModel, String packDir, Path targetFolder, String tplRoot, String... tpls) {
		try {
			Context<GenModel> context = Context.of(GenModel.class, genModel);

			String genTplRoot = getGenTplRoot();
			String name = genModel.name;

			for (String tpl : tpls) {

				String className = tpl.contains(".java") ? genModel.className : name;

				String file = tpl //
						.replace("$pack", genModel.pack.replace('.', '/'))
						.replace("$name", className)
						.replace(".tpl", "");

				Path targetFile = targetFolder.resolve(packDir)
						.resolve(file);

				Files.createDirectories(targetFile.getParent());

				Config config = new Config();
				if (!tpl.contains(".html")) {
					config.contentType = "text/plain";
				}

				try (Writer out = Files.newBufferedWriter(targetFile, StandardCharsets.UTF_8)) {
					log.accept(format("generating artifact '%s'...", targetFile));
					Ngoy.renderTemplate(format("/%s/tpl/%s/%s", genTplRoot, tplRoot, tpl), context, out, config);
				}
			}
		} catch (Exception e) {
			throw new NgoyException(e, "Error while generating artifact");
		}
	}

	private String getGenTplRoot() {
		return getClass().getPackage()
				.getName()
				.replace('.', '/');
	}

	public Consumer<String> getLog() {
		return log;
	}

	public void setLog(Consumer<String> log) {
		this.log = log == null ? NIRVANA : log;
	}

	private void initGit(Path targetFolder) {
		boolean gitReady = false;
		try {
			runProcess(targetFolder, "git", "--version");
			gitReady = true;
		} catch (Exception e) {
			// ignore
		}

		if (!gitReady) {
			return;
		}

		log.accept("Initializing git...");

		try {
			runProcess(targetFolder, "git", "init");
			runProcess(targetFolder, "git", "add", "*");
			runProcess(targetFolder, "git", "commit", "-m", "initial add");
		} catch (Exception e) {
			log.accept(format("Error while initializing git: %s", String.valueOf(e.getMessage())));
		}
	}

	protected void runProcess(Path cwd, String... args) throws Exception {
		new ProcessBuilder(args) //
				.directory(cwd.toFile())
				.start()
				.waitFor();
	}
}
