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

public class ForOfTest extends ANgoyTest {

	private TestService<List<Person>> personService;

	@Before
	public void beforeEach() {
		personService = TestService.of(asList(new Person("peter"), new Person("paul"), new Person("mary")));
	}

	@Component(selector = "test", template = "<person *ngFor=\"let it of persons; index as i; first  as f; last as l; even as e; odd as o\" [person]=\"it\" [pi]=\"i\" [pf]=\"f\" [pl]=\"l\" [pe]=\"e\" [po]=\"o\"></person>")
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
		assertThat(render(CmpForOf.class, useValue(TestService.class, personService))).isEqualTo(
				"<person pi=\"0\" pf=\"true\" pl=\"false\" pe=\"true\" po=\"false\">hello: peter</person><person pi=\"1\" pf=\"false\" pl=\"false\" pe=\"false\" po=\"true\">hello: paul</person><person pi=\"2\" pf=\"false\" pl=\"true\" pe=\"true\" po=\"false\">hello: mary</person>");
	}
}
