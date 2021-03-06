package ngoy.common.cmp;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

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

	@Component(selector = "", template = "hello {{getBetween(1).persons.get(0).getName()}}")
	public static class ListGetCmp {
		public Between getBetween(@SuppressWarnings("unused") int i) {
			return new Between();
		}
	}

	@Test
	public void testListGet() {
		assertThat(render(ListGetCmp.class)).isEqualTo("hello Paul");
	}

	//

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

	@Component(selector = "", template = "hello {{NAME}} {{doIt()}} <ng-container *ngFor='let c of colors'>{{c}}</ng-container>")
	public static class StaticCmp {
		public static enum Color {
			RED, GREEN, BLUE
		}

		public static String doIt() {
			return "justDoIt";
		}

		public static Stream<Color> getColors() {
			return Stream.of(Color.values())
					.filter(c -> c != Color.RED);
		}

		public static final String NAME = "world";
	}

	@Test
	public void testStatic() {
		assertThat(render(StaticCmp.class)).isEqualTo("hello world justDoIt GREENBLUE");
	}

	//

	@Component(selector = "", template = "{{messages.isEmpty()}}")
	public static class IsEmptyCmp {
		public List<String> messages = asList("hello");
	}

	@Test
	public void testIsEmpty() {
		assertThat(render(IsEmptyCmp.class)).isEqualTo("false");
	}

	//

	@Component(selector = "", template = "{{messages.get(0).trim()}}")
	public static class SimpleCmp {
		public List<String> messages = asList("hello ");
	}

	@Test
	public void testSimple() {
		assertThat(render(SimpleCmp.class)).isEqualTo("hello");
	}

	//

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
