package ngoy.internal.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ngoy.internal.cli.Cli;

public class CliTest {

	private ByteArrayOutputStream out;
	private PrintStream prevOut;
	private PrintStream prevErr;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void beforeEach() throws Exception {
		out = new ByteArrayOutputStream();
		prevOut = System.out;
		prevErr = System.err;
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(out));
	}

	@After
	public void afterEach() throws Exception {
		System.setOut(prevOut);
		System.setErr(prevErr);
	}

	private String run(String... args) {
		new Cli().run(args, out);
		String result = new String(out.toByteArray());
		result = result.replaceAll("\\r|\\n", "");
		return result;
	}

	@Test
	public void testUsage() {
		assertThat(run()).contains("usage:");
	}

	@Test
	public void testHelp() {
		assertThat(run("-h")).contains("usage:");
	}

	@Test
	public void testVar() {
		assertThat(run("-v", "x=1", "-v", "a=3", "-e", "Int(x) + Int(a)")).isEqualTo("4");
	}

	@Test
	public void testFile() throws Exception {
		File file = folder.newFile();
		Files.write(file.toPath(), "Int(x) + Int(a)".getBytes("UtF-8"));
		assertThat(run("-vx=1", "-va=3", "-e", "-f", file.getAbsolutePath())).isEqualTo("4");
	}

	@Test
	public void testError() {
		assertThat(run("-x")).isEqualTo("Unrecognized option: -x");
	}

	@Test
	public void testVersion() {
		assertThat(run("--version")).isEqualTo("unknown");
	}

	@Test
	public void test() {
		prevOut.print(run());
	}
}
