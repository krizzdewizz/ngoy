package ngoy.common.cmp;

import static java.util.Arrays.asList;
import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.common.cmp.CmpTest.PersonCmp;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.model.Person;
import ngoy.service.TestService;

public class ForOfNestedTest extends ANgoyTest {

	private TestService<List<Person>> personService;

	@Before
	public void beforeEach() {
		personService = TestService.of(asList(new Person("peter"), new Person("paul"), new Person("mary")));
	}

	@Component(selector = "persons", template = "<b *ngFor=\"let it of getPersons()\" [class.selected]=\"selectedPerson == it\">{{it.getName()}}</b>")
	public static class PersonsCmp {
		@Inject
		public TestService<List<Person>> service;

		public Person selectedPerson;

		public List<Person> getPersons() {
			return service.value;
		}
	}

	@Component(selector = "test", template = "<persons></persons>")
	@NgModule(declarations = { PersonCmp.class, PersonsCmp.class })
	public static class CmpForOfNested {
	}

	@Test
	public void testForOfNested() {
		assertThat(render(CmpForOfNested.class, useValue(TestService.class, personService))).isEqualTo("<persons><b>peter</b><b>paul</b><b>mary</b></persons>");
	}
}
