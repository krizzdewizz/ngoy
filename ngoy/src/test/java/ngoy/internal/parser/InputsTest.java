package ngoy.internal.parser;

import static java.util.Arrays.asList;
import static ngoy.core.Provider.useValue;
import static ngoy.internal.parser.Inputs.fieldName;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.Injector;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.model.Person;
import ngoy.service.TestService;

public class InputsTest extends ANgoyTest {

	@Test
	public void test() {
		assertThat(fieldName("setName")).isEqualTo("name");
		assertThat(fieldName("name")).isEqualTo("name");
		assertThat(fieldName("set")).isEqualTo("set");
	}

	//

	@Component(selector = "person", template = "hello: {{person == null ? 'unknown' : person.name}}")
	public static class PersonCmp {
		@Input()
		public Person person;

		@Input()
		public Boolean aBoolean;

		@Input()
		public boolean aboolean;

		@Input()
		public int aint;

		@Input()
		public long along;

		@Input()
		public short ashort;

		@Input()
		public byte abyte;

		@Input()
		public char achar;

		@Input()
		public float afloat;

		@Input()
		public double adouble;
	}

	@Component(selector = "test", template = "<person [person]=\"service.value[0]\"></person><person></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Cmp {

		@Inject
		public TestService<Person> service;
	}

	@Test
	public void testInputReset() {
		TestService<List<Person>> personService = TestService.of(asList(new Person("peter")));
		PersonCmp personCmp = new PersonCmp();

		Injector inj = new Injector() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> T get(Class<T> clazz) {
				return clazz == PersonCmp.class ? (T) personCmp : null;
			}
		};
		assertThat(render(Cmp.class, builder -> builder.injectors(inj), useValue(TestService.class, personService))).isEqualTo("<person>hello: peter</person><person>hello: unknown</person>");
	}

	//

	@Component(selector = "test", template = "")
	public static class ErrInitCmp {
		@Input
		public boolean ok = true;
	}
}
