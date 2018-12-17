package ngoy.common.cmp;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgoyException;
import ngoy.model.Person;

public class FieldAccessTest extends ANgoyTest {

	public static class Between {
		public List<Person> getPersons() {
			return asList(new Person("Paul", 21), new Person("Mary", 19));
		}
	}

	@Component(selector = "", template = "hello {{person.name}} <b *ngFor='let p of getBetween(1).persons'>{{p.name}},{{p.teenager}}</b>")
	public static class TestCmp {
		private Person person = new Person("Peter", 22);

		public Person getPerson() {
			return person;
		}

		public Between getBetween(@SuppressWarnings("unused") int i) {
			return new Between();
		}
	}

	@Test
	public void testIterables() {
		assertThat(render(TestCmp.class)).isEqualTo("hello Peter <b>Paul,false</b><b>Mary,true</b>");
	}

	//

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Component(selector = "", template = "<b *ngFor='let p of s'></b>")
	public static class NotIterableCmp {
		public String s;
	}

	@Test
	public void test() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("'s' is not iterable"));
		render(NotIterableCmp.class);
	}
}
