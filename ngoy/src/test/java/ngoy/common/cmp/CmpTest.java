package ngoy.common.cmp;

import static java.util.Arrays.asList;
import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.model.Person;
import ngoy.service.TestService;

public class CmpTest extends ANgoyTest {

	private TestService<List<Person>> personService;

	@Before
	public void beforeEach() {
		personService = TestService.of(asList(new Person("peter"), new Person("paul"), new Person("mary")));
	}

	@Component(selector = "person", template = "hello: {{person.name}}")
	public static class PersonCmp {
		@Input()
		public Person person;
	}

	@Component(selector = "test", template = "<person [person]=\"persons[0]\"></person><person [person]=\"persons[1]\"></person><person [person]=\"persons[2]\"></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Cmp {

		private TestService<List<Person>> service;

		@Inject
		public void setSetIt(TestService<List<Person>> service) {
			this.service = service;
		}

		public List<Person> getPersons() {
			return service.value;
		}
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class, useValue(TestService.class, personService))).isEqualTo("<person>hello: peter</person><person>hello: paul</person><person>hello: mary</person>");
	}
}
