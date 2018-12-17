package ngoy.internal.parser;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static ngoy.internal.parser.FieldAccessToGetterParser.fieldAccessToGetter;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ngoy.internal.parser.FieldAccessToGetterParser.ClassDef;
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
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "name", emptyMap(), null)).isEqualTo("getName()");
	}

	@Test
	public void nested() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "name.xxx", emptyMap(), null)).isEqualTo("getName().getXxx()");
	}

	@Test
	public void nestedWithField() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "qbert.person.name", emptyMap(), null)).isEqualTo("getQbert().person.getName()");
	}

	@Test
	public void between() {
		ClassDef[] outLastClassDef = new ClassDef[1];
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "getBetween().persons", emptyMap(), outLastClassDef)).isEqualTo("getBetween().getPersons()");
		assertThat(outLastClassDef[0].clazz).isEqualTo(List.class);
		assertThat(outLastClassDef[0].genericType.getTypeName()).isEqualTo(format("%s<%s>", List.class.getName(), Person.class.getName()));
	}

	@Test
	public void unknown() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "qbert.personx.name", emptyMap(), null)).isEqualTo("getQbert().personx.name");
	}

	@Test
	public void otherVariable() {
		Map<String, Class<?>> vars = new HashMap<>();
		vars.put("x", Person.class);
		assertThat(fieldAccessToGetter(MyCmp.class, vars, "qbert.foo(x.name)", emptyMap(), null)).isEqualTo("getQbert().foo(x.getName())");
	}
}