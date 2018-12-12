package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;

public class InputTest extends ANgoyTest {

	@Component(selector = "person", template = "hello:{{name}}{{name2}}{{namey}}{{qbert}}")
	public static class PersonCmp {
		@Input
		public String name;

		@Input("namex")
		public String name2;

		public String namey;

		@Input()
		public void setNameY(String value) {
			namey = value;
		}

		public String qbert;

		@Input("qbertInTheSky")
		public void doesNotMatter(String value) {
			qbert = value;
		}
	}

	@Component(selector = "test", template = "<person [name]='\"a\"' [namex]='\"b\"' [nameY]='\"c\"' [qbertInTheSky]='\"d\"'></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<person>hello:abcd</person>");
	}
}
