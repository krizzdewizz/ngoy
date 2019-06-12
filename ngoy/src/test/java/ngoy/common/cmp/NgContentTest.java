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

public class NgContentTest extends ANgoyTest {

	private TestService<Person> personService;

	@Before
	public void beforeEach() {
		personService = TestService.of(new Person("mary"));
	}

	@Component(selector = "person", template = "hello: {{person.getName()}}<ng-content></ng-content>x")
	public static class PersonCmp {
		@Input()
		public Person person;
	}

	@Component(selector = "test", template = "<person [person]=\"getPerson()\"><hr [class.abc]=\"getX()\"></person>")
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

	//

	@Component(selector = "ngoy-title", template = "<h1><ng-content></ng-content></h1>")
	public static class TitleCmp {
	}

	@Component(selector = "test", template = "<ngoy-title>hello</ngoy-title>")
	@NgModule(declarations = { TitleCmp.class })
	public static class QCmp {
	}

	@Test
	public void testQ() {
		assertThat(render(QCmp.class)).isEqualTo("<ngoy-title><h1>hello</h1></ngoy-title>");
	}

}
