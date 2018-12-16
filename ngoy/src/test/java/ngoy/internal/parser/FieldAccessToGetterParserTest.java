package ngoy.internal.parser;

import static java.util.Collections.emptyMap;
import static ngoy.internal.parser.FieldAccessToGetterParser.fieldAccessToGetter;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ngoy.model.Person;

public class FieldAccessToGetterParserTest {

	private interface MyName {
		String getXxx();
	}

	private class Qbert {
		@SuppressWarnings("unused")
		public Person person;
	}

	private static class MyCmp {
		@SuppressWarnings("unused")
		public MyName getName() {
			return null;
		}

		@SuppressWarnings("unused")
		public Qbert getQbert() {
			return null;
		}
	}

	@Test
	public void simple() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "name")).isEqualTo("getName()");
	}

	@Test
	public void nested() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "name.xxx")).isEqualTo("getName().getXxx()");
	}

	@Test
	public void nestedWithField() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "qbert.person.name")).isEqualTo("getQbert().person.getName()");
	}

	@Test
	public void unknown() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "qbert.personx.name")).isEqualTo("getQbert().personx.name");
	}

	@Test
	public void otherVariable() {
		Map<String, Class<?>> vars = new HashMap<>();
		vars.put("x", Person.class);
		assertThat(fieldAccessToGetter(MyCmp.class, vars, "qbert.foo(x.name)")).isEqualTo("getQbert().foo(x.getName())");
	}
}
