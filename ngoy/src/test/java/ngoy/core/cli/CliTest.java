package ngoy.core.cli;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ngoy.core.NgoyException;

public class CliTest {

	private ByteArrayOutputStream out;
	private PrintStream prevOut;
	private PrintStream prevErr;
	private InputStream prevIn;
	private ngoy.core.gen.Cli genCli;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void beforeEach() throws Exception {
		prevOut = System.out;
		prevErr = System.err;
		prevIn = System.in;
	}

	@After
	public void afterEach() throws Exception {
		System.setOut(prevOut);
		System.setErr(prevErr);
		System.setIn(prevIn);
		genCli = null;
	}

	private void resetOut() {
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(out));
	}

	private String run(String... args) {
		return run(false, args);
	}

	private String run(boolean rawOutput, String... args) {
		resetOut();
		Writer outt = new OutputStreamWriter(out, StandardCharsets.UTF_8);
		new Cli() {
			@Override
			protected ngoy.core.gen.Cli createGenCli() {
				return genCli != null ? genCli : super.createGenCli();
			}
		}.run(args, outt);
		try {
			outt.flush();
		} catch (IOException e) {
			throw NgoyException.wrap(e);
		}
		String result = new String(out.toByteArray());
		if (!rawOutput) {
			result = result.replaceAll("\\r|\\n", "");
		}
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
	public void testNewLine() {
		assertThat(run(true, "{{\"\\n\"}}")).isEqualTo("\n");
	}

	@Test
	public void testNewLineExpr() {
		assertThat(run(true, "-e", "'\\n'")).isEqualTo("\n");
	}

	@Test
	public void testInput() {
		System.setIn(new ByteArrayInputStream("line1\nline2\n".getBytes()));
		String sep = System.lineSeparator();
		assertThat(run(true, "-e", "-in", "$ + 'a' + nl")).isEqualTo(format("line1a%sline2a%s", sep, sep));
	}

	@Test
	public void testPipe() {
		assertThat(run("-e", "\"hello\" | uppercase")).isEqualTo("HELLO");
	}

	@Test
	public void testNew() {
		genCli = new ngoy.core.gen.Cli() {
			@Override
			public void run(String[] args, Writer out) {
				try {
					out.write(asList(args).toString());
				} catch (IOException e) {
					throw NgoyException.wrap(e);
				}
			}
		};
		assertThat(run("new", "org.myapp.Qbert")).isEqualTo("[project, org.myapp.Qbert]");
	}

	@Test
	public void testGen() {
		genCli = new ngoy.core.gen.Cli() {
			@Override
			public void run(String[] args, Writer out) {
				try {
					out.write(asList(args).toString());
				} catch (IOException e) {
					throw NgoyException.wrap(e);
				}
			}
		};
		assertThat(run("g", "c", "person", "-p", "abc")).isEqualTo("[c, person, -p, abc]");
	}

	@Test
	public void test() {
		prevOut.print(run());
	}
}
