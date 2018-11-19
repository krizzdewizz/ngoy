package ngoy;

import static java.util.Arrays.asList;
import static ngoy.core.NgoyException.wrap;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

import ngoy.Ngoy;
import ngoy.Ngoy.Config;
import ngoy.core.Context;

public class RenderStringTest {

	private static class Out extends OutputStream {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		@Override
		public String toString() {
			try {
				return new String(out.toByteArray(), "UTF-8");
			} catch (Exception e) {
				throw wrap(e);
			}
		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
		}
	}

	@Test
	public void test() {
		Config config = new Config();
		config.contentType = "text/plain";
		Context ctx = Context.of("all", asList(11, 22, 33));
		Out out = new Out();
		Ngoy.renderString("<ng-container *ngFor=\"let x of all; index as i\">hello {{x}}, {{i}}\n</ng-container>", ctx, out, config);
		assertThat(out.toString()).isEqualTo("hello 11, 0\nhello 22, 1\nhello 33, 2\n");
	}

	@Test
	public void testMicroSyntax() {
		Config config = new Config();
		config.contentType = "text/plain";
		Context ctx = Context.of("all", asList(11, 22, 33));
		Out out = new Out();
		Ngoy.renderString("*ngFor let x of all; index as i:hello {{x}}, {{i}}\n", ctx, out, config);
		assertThat(out.toString()).isEqualTo("hello 11, 0\nhello 22, 1\nhello 33, 2\n");
	}

	public class Person {
		private final String name;

		public Person(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	@Test
	public void some() {
		Ngoy.renderString("hello: {{name}}", Context.of("name", "peter"), System.out);
		Ngoy.renderString("hello: {{name}}", Context.of(new Person("sam")), System.out);
	}
}
