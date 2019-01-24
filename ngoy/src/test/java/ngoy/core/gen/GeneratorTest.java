package ngoy.core.gen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import ngoy.core.NgoyException;
import ngoy.core.Util;

public class GeneratorTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	private static final String TEST_VERSION = "1.0.0-rc3";

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private Generator generator;

	@Before
	public void beforeEach() {
		generator = new Generator() {
			@Override
			protected void runProcess(Path cwd, String... args) throws Exception {
				// do nothing
			}
		};
	}

	@Test
	public void testComponent() throws Exception {
		GenModel genModel = new GenModel("app", "org.qbert.heroes_detail.HeroesDetail", TEST_VERSION);
		File fldr = folder.newFolder();

		generator.component(genModel, fldr.toPath());

		Path packFolder = fldr.toPath()
				.resolve("org/qbert/heroes_detail");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(3);
		try (InputStream in = new FileInputStream(packFolder.resolve("HeroesDetailComponent.java")
				.toFile())) {
			String cmp = Util.copyToString(in);
			assertThat(cmp).contains("public class HeroesDetailComponent");
			assertThat(cmp).contains("heroes-detail.component.html");
			assertThat(cmp).contains("heroes-detail.component.css");
			assertThat(cmp).contains("selector = \"app-heroes-detail\"");
		}

		try (InputStream in = new FileInputStream(packFolder.resolve("heroes-detail.component.html")
				.toFile())) {
			String html = Util.copyToString(in);
			assertThat(html).contains("heroes-detail works!");
		}
	}

	@Test
	public void testDirective() throws Exception {
		GenModel genModel = new GenModel("app", "org.qbert.UpperCase", TEST_VERSION);
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
		GenModel genModel = new GenModel("app", "org.qbert.WesternCity", TEST_VERSION);
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
	public void testMod() throws Exception {
		GenModel genModel = new GenModel("app", "org.qbert.ClockWise", TEST_VERSION);
		File fldr = folder.newFolder();

		generator.mod(genModel, fldr.toPath());

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

	@Test
	public void testProjectNotEmptyFolder() throws Exception {
		GenModel genModel = new GenModel("app", "org.qbert.ClockWise", TEST_VERSION);
		Path fldr = folder.newFolder()
				.toPath();

		Files.write(fldr.resolve("dummy"), "".getBytes());

		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Target folder must be empty"));
		expectedEx.expectMessage(containsString(fldr.toString()));
		generator.project(genModel, fldr);
	}

	@Test
	public void testProject() throws Exception {
		GenModel genModel = new GenModel("app", "org.qbert.ClockWise", TEST_VERSION);
		Path fldr = folder.newFolder()
				.toPath();

		// fldr = new File("d:/downloads/qbert/a");

		generator.project(genModel, fldr);

		assertThat(fldr.toFile()
				.listFiles()).hasSize(6);
		assertThat(fldr.resolve("src/main/java/org/qbert")
				.toFile()
				.listFiles()).hasSize(2);
		assertThat(fldr.resolve("src/main/resources")
				.toFile()
				.listFiles()).hasSize(2);
		assertThat(fldr.resolve("src/main/java/org/qbert/app")
				.toFile()
				.listFiles()).hasSize(4);
	}
}
