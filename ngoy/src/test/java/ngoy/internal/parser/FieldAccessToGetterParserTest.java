package ngoy.internal.parser;

import static ngoy.internal.parser.FieldAccessToGetterParser.fieldAccessToGetter;
import static org.assertj.core.api.Assertions.assertThat;

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
		assertThat(fieldAccessToGetter(MyCmp.class, "name")).isEqualTo("getName()");
	}

	@Test
	public void nested() {
		assertThat(fieldAccessToGetter(MyCmp.class, "name.xxx")).isEqualTo("getName().getXxx()");
	}

	@Test
	public void nestedWithField() {
		assertThat(fieldAccessToGetter(MyCmp.class, "qbert.person.name")).isEqualTo("getQbert().person.getName()");
	}

	@Test
	public void unknown() {
		assertThat(fieldAccessToGetter(MyCmp.class, "qbert.personx.name")).isEqualTo("getQbert().personx.name");
	}
}
