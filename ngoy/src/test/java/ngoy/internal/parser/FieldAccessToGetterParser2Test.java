package ngoy.internal.parser;

import static java.util.Collections.emptyMap;
import static ngoy.internal.parser.FieldAccessToGetterParser.fieldAccessToGetter;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import ngoy.model.Person;

@SuppressWarnings("unused")
public class FieldAccessToGetterParser2Test {

	private static class Between {
		public List<Person> persons;
	}

	private static class MyCmp {
		public List<Person> getPersons() {
			return null;
		}

		public List<Person> persons2;
		public Between between;

		public Map<String, Person> personMap;
	}

	@Test
	public void getter() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "persons.get(0).name", emptyMap(), null)).isEqualTo("((ngoy.model.Person) persons.get(0)).getName()");
	}

	@Test
	public void field() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "persons2.get(0).name", emptyMap(), null)).isEqualTo("((ngoy.model.Person) persons2.get(0)).getName()");
	}

	@Test
	public void fieldBetween() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "between.persons.get(0).name", emptyMap(), null)).isEqualTo("((ngoy.model.Person) between.persons.get(0)).getName()");
	}

	@Test
	public void map() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "personMap.get('x').name", emptyMap(), null)).isEqualTo("((ngoy.model.Person) personMap.get('x')).getName()");
	}

	@Test
	public void mapValues() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "personMap.values().next().name", emptyMap(), null)).isEqualTo("((ngoy.model.Person) personMap.values().next()).getName()");
	}
}
