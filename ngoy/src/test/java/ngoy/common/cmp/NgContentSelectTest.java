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

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

public class NgContentSelectTest extends ANgoyTest {

	private TestService<Person> personService;

	@Before
	public void beforeEach() {
		personService = TestService.of(new Person("mary"));
	}

	@Component(selector = "person", template = "hello: {{person.getName()}}<ng-content select=\"[abc]\"></ng-content>x")
	public static class PersonCmp {
		@Input()
		public Person person;
	}

	@Component(selector = "test", template = "<person [person]=\"getPerson()\"><hr><span abc>kuckuck</span></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Cmp {
		@Inject
		public TestService<Person> service;

		public boolean getX() {
			return true;
		}

		public Person getPerson() {
			return service.value;
		}
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class, useValue(TestService.class, personService))).isEqualTo("<person>hello: mary<span abc>kuckuck</span>x</person>");
	}
}
