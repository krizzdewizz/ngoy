package org.ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Provider.useValue;

import org.junit.Before;
import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.Inject;
import org.ngoy.core.Input;
import org.ngoy.core.NgModule;
import org.ngoy.model.Person;
import org.ngoy.service.TestService;

public class NgContentTest extends ANgoyTest {

	private TestService<Person> personService;

	@Before
	public void beforeEach() {
		personService = TestService.of(new Person("mary"));
	}

	@Component(selector = "person", template = "hello: {{person.name}}<ng-content></ng-content>x")
	public static class PersonCmp {
		@Input()
		public Person person;
	}

	@Component(selector = "test", template = "<person [person]=\"person\"><hr [class.abc]=\"x\"></person>")
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
		assertThat(render(Cmp.class, useValue(TestService.class, personService))).isEqualTo("<person>hello: mary<hr class=\"abc\">x</person>");
	}
}
