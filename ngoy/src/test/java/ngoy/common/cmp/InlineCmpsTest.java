package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.model.Person;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InlineCmpsTest extends ANgoyTest {

	@Component(selector = "person", template = "hello: {{person.getName()}}")
	public static class PersonCmp {
		@Input()
		public Person person;
	}

	@Component(selector = "test", template = "~<a>~<person [person]=\"peter\"></person>~</a>~")
	@NgModule(declarations = { PersonCmp.class })
	public static class Cmp {
		public Person peter = new Person("peter", 22);
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class, builder -> builder.inlineComponents(true))).isEqualTo("~<a>~hello: peter~</a>~");
	}
}
