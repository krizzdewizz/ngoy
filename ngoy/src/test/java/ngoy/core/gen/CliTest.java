package ngoy.core.gen;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CliTest {
	private ByteArrayOutputStream out;
	private PrintStream prevOut;
	private PrintStream prevErr;
	private InputStream prevIn;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private Path cwd;
	private Cli cli;

	@Before
	public void beforeEach() throws Exception {
		prevOut = System.out;
		prevErr = System.err;
		prevIn = System.in;
//		cwd = Paths.get("d:\\temp\\aaa");
		cwd = folder.newFolder()
				.toPath();
		cli = new Cli() {
			@Override
			protected Path getCwd() {
				return cwd;
			}

			@Override
			protected Path getPropertiesPath() {
				return cwd.resolve("ngoy.properties");
			}
		};
	}

	@After
	public void afterEach() throws Exception {
		System.setOut(prevOut);
		System.setErr(prevErr);
		System.setIn(prevIn);
	}

	private void resetOut() {
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(out));
	}

	private String run(String... args) throws Exception {
		resetOut();
		cli.run(args, new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
		return new String(out.toByteArray());
	}

	@Test
	public void testUsage() throws Exception {
		assertThat(run()).contains("usage:");
		assertThat(run("komponent")).contains("usage:");
		assertThat(run("c", "org.qbert.X")).doesNotContain("usage:");
	}

	@Test
	public void testComponentNoPackage() throws Exception {
		run("c", "Person");

		assertThat(cwd.toFile()
				.listFiles()).hasSize(3);
	}

	@Test
	public void testComponentPackage() throws Exception {
		run("c", "org.qbert.Person");

		Path packFolder = cwd.resolve("org/qbert");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(3);
	}

	@Test
	public void testComponentAppPrefix() throws Exception {

		Files.write(cli.getPropertiesPath(), "app.prefix=myapp".getBytes());

		run("c", "person.Person");

		Path packFolder = cwd.resolve("person");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(3);

		String cmp = new String(Files.readAllBytes(packFolder.resolve("PersonComponent.java")));
		assertThat(cmp).contains("selector = \"myapp-person\"");
	}

	@Test
	public void testPipeSrcFolder() throws Exception {

		Path mainJava = cwd.resolve("src/main/java");
		Files.createDirectories(mainJava);

		run("pi", "org.qbert.Person");

		Path packFolder = cwd.resolve("src/main/java/org/qbert");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(1);
	}

	@Test
	public void testPipeSrcFolderParam() throws Exception {

		run("pi", "org.qbert.Person", "-t", "abc/def");

		Path packFolder = cwd.resolve("abc/def/org/qbert");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(1);
	}
}
