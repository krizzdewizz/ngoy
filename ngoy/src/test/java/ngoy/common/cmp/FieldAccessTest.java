package ngoy.common.cmp;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.model.Person;

public class FieldAccessTest extends ANgoyTest {

	@Component(selector = "", template = "hello {{person.name}} <b *ngFor='let p of persons'>{{p.name}}</b>")
	public static class TestCmp {
		private Person person = new Person("Peter", 22);
		private List<Person> persons = asList(person, new Person("Mary", 33));

		public Person getPerson() {
			return person;
		}

		public List<Person> getPersons() {
			return persons;
		}
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("hello Peter <b>Peter</b><b>Mary</b>");
	}
}
