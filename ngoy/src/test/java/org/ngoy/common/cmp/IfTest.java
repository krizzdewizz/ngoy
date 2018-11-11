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
import org.ngoy.core.NgModule;
import org.ngoy.model.Person;
import org.ngoy.service.TestService;

public class IfTest extends ANgoyTest {

	private TestService<List<Person>> personService;

	@Before
	public void beforeEach() {
		personService = TestService.of(asList(new Person("peter"), new Person("paul"), new Person("mary")));
	}

	@Component(selector = "person", template = "hello: {{person.name}} <span *ngIf=\"mary\">do you have a little lamb?</span>")
	public static class PersonCmp {
		@Input()
		public Person person;

		public boolean isMary() {
			return "mary".equals(person.getName());
		}
	}

	@Component(selector = "test", template = "<person *ngFor=\"let it of persons\" [person]=\"it\"></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class CmpIf {
		@Inject
		public TestService<List<Person>> service;

		public List<Person> getPersons() {
			return service.value;
		}
	}

	@Test
	public void testIf() {
		assertThat(render(CmpIf.class, useValue(TestService.class, personService)))
				.isEqualTo("<person>hello: peter </person><person>hello: paul </person><person>hello: mary <span>do you have a little lamb?</span></person>");
	}

	//

	@Component(selector = "person", template = "<ng-template #x>xyz</ng-template>hello: {{person.name}} <span *ngIf=\"mary; else x\">do you have a little lamb?</span>")
	public static class PersonIfElseCmp {
		@Input()
		public Person person;

		public boolean isMary() {
			return "mary".equals(person.getName());
		}
	}

	@Component(selector = "test", template = "<person *ngFor=\"let it of persons\" [person]=\"it\"></person>")
	@NgModule(declarations = { PersonIfElseCmp.class })
	public static class CmpIfElse {
		@Inject
		public TestService<List<Person>> service;

		public List<Person> getPersons() {
			return service.value;
		}
	}

	@Test
	public void testIfElse() {
		assertThat(render(CmpIfElse.class, useValue(TestService.class, personService)))
				.isEqualTo("<person>hello: peter xyz</person><person>hello: paul xyz</person><person>hello: mary <span>do you have a little lamb?</span></person>");
	}
}
