package ngoy;

import ngoy.Ngoy.Config;
import ngoy.core.Context;
import org.junit.Test;

import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RenderStringTest {

	@Test
	public void test() {
		Config config = new Config();
		config.contentType = "text/plain";
		Context<?> ctx = Context.of("all", List.class, asList(11, 22, 33));
		StringWriter out = new StringWriter();
		Ngoy.renderString("<ng-container *ngFor=\"let x of all; index as i\">hello {{x}}, {{i}}\n</ng-container>", ctx, out, config);
		assertThat(out.toString()).isEqualTo("hello 11, 0\nhello 22, 1\nhello 33, 2\n");
	}

	@Test
	public void testMicroSyntax() {
		Config config = new Config();
		config.contentType = "text/plain";
		Context<?> ctx = Context.of("all", List.class, asList(11, 22, 33));
		StringWriter out = new StringWriter();
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
//		Ngoy.renderString("hello: {{name}}", Context.of("name", String.class, "peter"), System.out);
		Ngoy.renderString("hello: {{getName()}}", Context.of(Person.class, new Person("sam")), new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
	}
}
