package ngoy.internal.parser;

import static java.util.Collections.emptyMap;
import static ngoy.internal.parser.FieldAccessToGetterParser.fieldAccessToGetter;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import ngoy.model.Person;

@SuppressWarnings("unused")
public class FieldAccessToGetterParser2Test {

	private static class Between {
		public List<Person> persons;
		public int theIndex;
	}

	private static class BetweenRecursive {
		public List<Person> getPersons() {
			return null;
		}

		public BetweenRecursive parent;
	}

	private static class MyCmp {
		public List<Person> getPersons() {
			return null;
		}

		public List<Person> persons2;
		public Between between;
		public BetweenRecursive betweenRecursive;

		public Between getBtw() {
			return null;
		}

		public Map<String, Person> personMap;
		public List<?> persons3;
		public List<? extends Person> persons4;
		public List<Between> betweens;
		public Map<Between, Person> betweenMap;
	}

	@Test
	public void simple() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "persons", emptyMap(), null, null)).isEqualTo("getPersons()");
	}

	@Test
	public void getter() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "persons.get(0).name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) getPersons().get(0)).getName()");
	}

	@Test
	public void arrayIndexForList() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "persons[0].name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) getPersons().get(0)).getName()");
	}

	@Test
	public void arrayIndexForList2() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "persons[btw.theIndex].name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) getPersons().get(getBtw().theIndex)).getName()");
	}

	@Test
	public void arrayIndexForMap() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "personMap['a'].name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) personMap.get(\"a\")).getName()");
	}

	@Test
	public void field() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "persons2.get(0).name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) persons2.get(0)).getName()");
	}

	@Test
	public void fieldBetween() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "between.persons.get(0).name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) between.persons.get(0)).getName()");
	}

	@Test
	public void map() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "personMap.get('x').name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) personMap.get(\"x\")).getName()");
	}

	@Test
	public void mapValues() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "personMap.values().next().name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) personMap.values().next()).getName()");
	}

	@Test
	public void wildcard() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "persons3.get(0).toString()", emptyMap(), null, null)).isEqualTo("persons3.get(0).toString()");
	}

	@Test
	public void wildcard2() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "persons4.get(0).name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) persons4.get(0)).getName()");
	}

	@Test
	public void betweens() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "betweens.get(0).persons.get(0)", emptyMap(), null, null)).isEqualTo("(ngoy.model.Person) ((ngoy.internal.parser.FieldAccessToGetterParser2Test.Between) betweens.get(0)).persons.get(0)");
	}

	@Test
	public void betweensWithFieldAccess() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "betweens.get(0).persons.get(0).name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) ((ngoy.internal.parser.FieldAccessToGetterParser2Test.Between) betweens.get(0)).persons.get(0)).getName()");
	}

	@Test
	public void betweenMap() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "betweenMap.get('a').name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) betweenMap.get(\"a\")).getName()");
	}

	@Test
	public void betweenKeySet() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "betweenMap.keySet().iterator().next().persons.get(0).name", emptyMap(), null, null))
				.isEqualTo("((ngoy.model.Person) ((ngoy.internal.parser.FieldAccessToGetterParser2Test.Between) betweenMap.keySet().iterator().next()).persons.get(0)).getName()");
	}

	@Test
	public void deep() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "betweenRecursive.parent.persons[0].name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) betweenRecursive.parent.getPersons().get(0)).getName()");
	}

	@Test
	public void deep2() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "betweenRecursive.persons[0].friends[0].name", emptyMap(), null, null)).isEqualTo("((ngoy.model.Person) (((ngoy.model.Person) betweenRecursive.getPersons().get(0)).getFriends()).get(0)).getName()");
	}
}
