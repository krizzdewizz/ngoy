package ngoy.core.gen;

import static java.lang.String.format;

import java.io.OutputStream;
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

	private final Config config = new Config();

	private Consumer<String> log = NIRVANA;

	public Generator() {
		config.contentType = "text/plain";
	}

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

	private void generateArtifacts(GenModel genModel, Path targetFolder, String tplRoot, String... tpls) {
		try {
			Context context = Context.of(genModel);

			String genTplRoot = getClass().getPackage()
					.getName()
					.replace('.', '/');

			String packDir = genModel.getPack()
					.replace('.', '/');

			for (String tpl : tpls) {
				String name = genModel.getName();

				String className = tpl.contains(".java") ? genModel.getClassName() : name;

				String file = tpl //
						.replace("$name", className)
						.replace(".tpl", "");

				Path targetFile = targetFolder.resolve(packDir)
						.resolve(file);

				Files.createDirectories(targetFile.getParent());

				try (OutputStream out = Files.newOutputStream(targetFile)) {
					log.accept(format("generating artifact '%s'...", targetFile));
					Ngoy.renderTemplate(format("/%s/tpl/%s/%s", genTplRoot, tplRoot, tpl), context, out, config);
				}
			}
		} catch (Exception e) {
			throw new NgoyException(e, "Error while generating artifact");
		}
	}

	public Consumer<String> getLog() {
		return log;
	}

	public void setLog(Consumer<String> log) {
		this.log = log == null ? NIRVANA : log;
	}
}
