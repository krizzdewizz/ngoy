package ngoy.internal.parser;

import static java.util.Collections.emptyMap;
import static ngoy.internal.parser.FieldAccessToGetterParser.fieldAccessToGetter;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ngoy.model.Person;

@SuppressWarnings("unused")
public class FieldAccessToGetterParserTest {

	private interface MyName {
		String getXxx();
	}

	private class Qbert {

		public Person person;
	}

	private class Between {
		public List<Person> getPersons() {
			return null;
		}
	}

	private static class MyCmp {
		public MyName getName() {
			return null;
		}

		public Qbert getQbert() {
			return null;
		}

		public Between getBetween() {
			return null;
		}
	}

	@Test
	public void simple() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "name", emptyMap(), null, null)).isEqualTo("getName()");
	}

	@Test
	public void getX() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "getBetween().persons.get(0).name", emptyMap(), null, null))
				.isEqualTo("((ngoy.model.Person) (getBetween().getPersons()).get(0)).getName()");
	}

	@Test
	public void nested() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "name.xxx", emptyMap(), null, null)).isEqualTo("getName().getXxx()");
	}

	@Test
	public void nestedWithField() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "qbert.person.name", emptyMap(), null, null)).isEqualTo("getQbert().person.getName()");
	}

	@Test
	public void between() {
		ClassDef[] outLastClassDef = new ClassDef[1];
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "getBetween().persons", emptyMap(), null, outLastClassDef)).isEqualTo("getBetween().getPersons()");
		assertThat(outLastClassDef[0].clazz).isEqualTo(List.class);
		assertThat(outLastClassDef[0].getTypeArgument()).isEqualTo(Person.class);
	}

	@Test
	public void between2() {
		ClassDef[] outLastClassDef = new ClassDef[1];
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "getBetween().persons.get(0).getName()", emptyMap(), null, outLastClassDef))
				.isEqualTo("((ngoy.model.Person) (getBetween().getPersons()).get(0)).getName()");
		assertThat(outLastClassDef[0].clazz).isEqualTo(String.class);
	}

	@Test
	public void unknown() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "qbert.personx.name", emptyMap(), null, null)).isEqualTo("getQbert().personx.name");
	}

	@Test
	public void otherVariable() {
		Map<String, Class<?>> vars = new HashMap<>();
		vars.put("x", Person.class);
		assertThat(fieldAccessToGetter(MyCmp.class, vars, "qbert.foo(x.name)", emptyMap(), null, null)).isEqualTo("getQbert().foo(x.getName())");
	}

	@Test
	public void builtInList() {
		ClassDef[] outLastClassDef = new ClassDef[1];
		fieldAccessToGetter(MyCmp.class, emptyMap(), "$list(1, 2, 3)", emptyMap(), null, outLastClassDef);
		assertThat(outLastClassDef[0].clazz).isEqualTo(List.class);
		assertThat(outLastClassDef[0].getTypeArgument()).isEqualTo(Integer.class);
	}

	@Test
	public void builtInMap() {
		ClassDef[] outLastClassDef = new ClassDef[1];
		fieldAccessToGetter(MyCmp.class, emptyMap(), "$map('a', 1, 'b', 2, 'c', 3)", emptyMap(), null, outLastClassDef);
		assertThat(outLastClassDef[0].clazz).isEqualTo(Map.class);
		assertThat(outLastClassDef[0].getTypeArgument()).isEqualTo(Integer.class);
	}
}
