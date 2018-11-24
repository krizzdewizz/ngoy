package ngoy.core.gen;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ngoy.core.Util;

public class GeneratorTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private Generator generator;

	@Before
	public void beforeEach() {
		generator = new Generator();
	}

	@Test
	public void testComponent() throws Exception {
		GenModel genModel = new GenModel("org.qbert", "heroes");
		File fldr = folder.newFolder();

		generator.component(genModel, fldr.toPath());

		Path packFolder = fldr.toPath()
				.resolve("org/qbert");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(3);
		try (InputStream in = new FileInputStream(packFolder.resolve("HeroesComponent.java")
				.toFile())) {
			String cmp = Util.copyToString(in);
			assertThat(cmp).contains("public class HeroesComponent");
			assertThat(cmp).contains("heroes.component.html");
			assertThat(cmp).contains("heroes.component.css");
		}

		try (InputStream in = new FileInputStream(packFolder.resolve("heroes.component.html")
				.toFile())) {
			String html = Util.copyToString(in);
			assertThat(html).contains("heroes works!");
		}
	}

	@Test
	public void testDirective() throws Exception {
		GenModel genModel = new GenModel("org.qbert", "upper-case");
		File fldr = folder.newFolder();

		generator.directive(genModel, fldr.toPath());

		Path packFolder = fldr.toPath()
				.resolve("org/qbert");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(1);
		try (InputStream in = new FileInputStream(packFolder.resolve("UpperCaseDirective.java")
				.toFile())) {
			String cmp = Util.copyToString(in);
			assertThat(cmp).contains("public class UpperCaseDirective");
			assertThat(cmp).contains("[appUpperCase]");
		}
	}

	@Test
	public void testPipe() throws Exception {
		GenModel genModel = new GenModel("org.qbert", "western-city");
		File fldr = folder.newFolder();

		generator.pipe(genModel, fldr.toPath());

		Path packFolder = fldr.toPath()
				.resolve("org/qbert");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(1);
		try (InputStream in = new FileInputStream(packFolder.resolve("WesternCityPipe.java")
				.toFile())) {
			String cmp = Util.copyToString(in);
			assertThat(cmp).contains("public class WesternCityPipe");
			assertThat(cmp).contains("@Pipe(\"western-city\")");
			assertThat(cmp).contains("implements PipeTransform");
		}
	}

	@Test
	public void testModule() throws Exception {
		GenModel genModel = new GenModel("org.qbert", "clock-wise");
		File fldr = folder.newFolder();

		generator.module(genModel, fldr.toPath());

		Path packFolder = fldr.toPath()
				.resolve("org/qbert");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(1);
		try (InputStream in = new FileInputStream(packFolder.resolve("ClockWiseModule.java")
				.toFile())) {
			String cmp = Util.copyToString(in);
			assertThat(cmp).contains("public class ClockWiseModule");
			assertThat(cmp).contains("@NgModule(");
		}
	}
}
