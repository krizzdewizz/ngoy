package ngoy.core.gen;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
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

	@Before
	public void beforeEach() throws Exception {
		prevOut = System.out;
		prevErr = System.err;
		prevIn = System.in;
//		cwd = Paths.get("d:\\temp\\aaa");
		cwd = folder.newFolder()
				.toPath();
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
		Cli cli = new Cli() {
			protected Path getCwd() {
				return cwd;
			}
		};
		cli.run(args, System.out);
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
		run("c", "person");

		Path packFolder = cwd.resolve("ngoygen/person");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(3);
	}

	@Test
	public void testComponentPackage() throws Exception {
		run("c", "-p", "org.qbert", "person");

		Path packFolder = cwd.resolve("org/qbert/person");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(3);
	}

	@Test
	public void testPipeSrcFolder() throws Exception {

		Path mainJava = cwd.resolve("src/main/java");
		Files.createDirectories(mainJava);

		run("p", "-p", "org.qbert", "person");

		Path packFolder = cwd.resolve("src/main/java/org/qbert");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(1);
	}

	@Test
	public void testPipeSrcFolderParam() throws Exception {

		run("p", "-p", "org.qbert", "-t", "abc/def", "person");

		Path packFolder = cwd.resolve("abc/def/org/qbert");
		assertThat(packFolder.toFile()
				.listFiles()).hasSize(1);
	}
}
