package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.model.Person;

public class FieldAccessTest extends ANgoyTest {

	@Component(selector = "", template = "hello {{person.name}}")
	public static class TestCmp {
		private Person person = new Person("Peter", 22);

		public Person getPerson() {
			return person;
		}
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("hello Peter");
	}
}
