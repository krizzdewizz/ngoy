package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.model.Person;
import ngoy.service.TestService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

public class CmpTest extends ANgoyTest {

	private TestService<List<Person>> personService;

	@Before
	public void beforeEach() {
		personService = TestService.of(asList(new Person("peter"), new Person("paul"), new Person("mary")));
	}

	@Component(selector = "person", template = "hello: {{person.getName()}}")
	public static class PersonCmp {
		@Input()
		public Person person;
	}

	@Component(selector = "test", template = "<person [person]='getPersons()[0]'></person><person [person]='getPersons()[1]'></person><person [person]='getPersons()[2]'></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class TestCmp {

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
		assertThat(render(TestCmp.class, useValue(TestService.class, personService))).isEqualTo("<person>hello: peter</person><person>hello: paul</person><person>hello: mary</person>");
	}

	@Component(selector = "test", template = "<person [person]='persons[0]'></person><person [person]='persons[1]'></person><person [person]='persons[2]'></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Test2Cmp {

		@Inject
		public void setSetIt(TestService<List<Person>> service) {
			this.persons = service.value;
		}

		public List<Person> persons;
	}

	@Test
	public void test2() {
		assertThat(render(Test2Cmp.class, useValue(TestService.class, personService))).isEqualTo("<person>hello: peter</person><person>hello: paul</person><person>hello: mary</person>");
	}
}
