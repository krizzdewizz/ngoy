package org.ngoy.common.cmp;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Provider.useValue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.Inject;
import org.ngoy.core.Input;
import org.ngoy.model.Person;
import org.ngoy.service.TestService;

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

	@Component(selector = "test", declarations = {
			PersonCmp.class }, template = "<person [person]=\"persons[0]\"></person><person [person]=\"persons[1]\"></person><person [person]=\"persons[2]\"></person>")
	public static class Cmp {
		@Inject
		public TestService<List<Person>> service;

		public List<Person> getPersons() {
			return service.value;
		}
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class, useValue(TestService.class, personService))).isEqualTo("<person>hello: peter</person><person>hello: paul</person><person>hello: mary</person>");
	}
}
