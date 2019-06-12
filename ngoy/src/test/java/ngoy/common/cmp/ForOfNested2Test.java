package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.common.cmp.CmpTest.PersonCmp;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.core.OnInit;
import ngoy.model.Person;
import ngoy.service.TestService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

public class ForOfNested2Test extends ANgoyTest {

	private TestService<List<Person>> personService;

	@Before
	public void beforeEach() {
		personService = TestService.of(asList(new Person("peter"), new Person("paul"), new Person("mary")));
	}

	@Component(selector = "persons", template = "<person *ngFor=\"let it of getPersons()\" [person]=\"it\" [class.active]=\"selectedPerson == it\"></person>")
	public static class PersonsCmp implements OnInit {
		@Inject
		public TestService<List<Person>> service;

		public Person selectedPerson;

		public List<Person> getPersons() {
			return service.value;
		}

		@Override
		public void onInit() {
			selectedPerson = getPersons().get(1);
		}
	}

	@Component(selector = "test", template = "<persons></persons>")
	@NgModule(declarations = { PersonCmp.class, PersonsCmp.class })
	public static class CmpForOfNested {
	}

	@Test
	public void testForOfNested2() {
		assertThat(render(CmpForOfNested.class, useValue(TestService.class, personService)))
				.isEqualTo("<persons><person>hello: peter</person><person class=\"active\">hello: paul</person><person>hello: mary</person></persons>");
	}
}
