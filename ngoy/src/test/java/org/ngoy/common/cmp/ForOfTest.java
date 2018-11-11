package org.ngoy.common.cmp;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Provider.useValue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.common.cmp.CmpTest.PersonCmp;
import org.ngoy.core.Component;
import org.ngoy.core.Inject;
import org.ngoy.core.NgModule;
import org.ngoy.model.Person;
import org.ngoy.service.TestService;

public class ForOfTest extends ANgoyTest {

	private TestService<List<Person>> personService;

	@Before
	public void beforeEach() {
		personService = TestService.of(asList(new Person("peter"), new Person("paul"), new Person("mary")));
	}

	@Component(selector = "test", template = "<person *ngFor=\"let it of persons\" [person]=\"it\"></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class CmpForOf {
		@Inject
		public TestService<List<Person>> service;

		public List<Person> getPersons() {
			return service.value;
		}
	}

	@Test
	public void testForOf() {
		assertThat(render(CmpForOf.class, useValue(TestService.class, personService))).isEqualTo("<person>hello: peter</person><person>hello: paul</person><person>hello: mary</person>");
	}
}
