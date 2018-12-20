package ngoy.internal.parser;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
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
		assertThat(ExprParser.prefixName(MyCmp.class, emptyMap(), "name", "", emptySet(), emptyMap(), null)).isEqualTo("getName()");
	}

	@Test
	public void getX() {
		assertThat(ExprParser.prefixName(MyCmp.class, emptyMap(), "getBetween().persons.get(0).name", "", emptySet(), emptyMap(), null))
				.isEqualTo("((ngoy.model.Person) (getBetween().getPersons()).get(0)).getName()");
	}

	@Test
	public void nested() {
		assertThat(ExprParser.prefixName(MyCmp.class, emptyMap(), "name.xxx", "", emptySet(), emptyMap(), null)).isEqualTo("getName().getXxx()");
	}

	@Test
	public void nestedWithField() {
		assertThat(ExprParser.prefixName(MyCmp.class, emptyMap(), "qbert.person.name", "", emptySet(), emptyMap(), null)).isEqualTo("getQbert().person.getName()");
	}

	@Test
	public void between() {
		ClassDef[] outLastClassDef = new ClassDef[1];
		assertThat(ExprParser.prefixName(MyCmp.class, emptyMap(), "getBetween().persons", "", emptySet(), emptyMap(), outLastClassDef)).isEqualTo("getBetween().getPersons()");
		assertThat(outLastClassDef[0].clazz).isEqualTo(List.class);
		assertThat(outLastClassDef[0].getTypeArgument()).isEqualTo(Person.class);
	}

	@Test
	public void between2() {
		ClassDef[] outLastClassDef = new ClassDef[1];
		assertThat(ExprParser.prefixName(MyCmp.class, emptyMap(), "getBetween().persons.get(0).getName()", "", emptySet(), emptyMap(), outLastClassDef))
				.isEqualTo("((ngoy.model.Person) (getBetween().getPersons()).get(0)).getName()");
		assertThat(outLastClassDef[0].clazz).isEqualTo(String.class);
	}

	@Test
	public void unknown() {
		assertThat(ExprParser.prefixName(MyCmp.class, emptyMap(), "qbert.personx.name", "", emptySet(), emptyMap(), null)).isEqualTo("getQbert().personx.name");
	}

	@Test
	public void otherVariable() {
		Map<String, Class<?>> vars = new HashMap<>();
		vars.put("x", Person.class);
		assertThat(ExprParser.prefixName(MyCmp.class, vars, "qbert.foo(x.name)", "", emptySet(), emptyMap(), null)).isEqualTo("getQbert().foo(x.getName())");
	}
}
