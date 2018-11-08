package org.ngoy.common.cmp;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.ngoy.common.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.Input;
import org.ngoy.model.Person;

public class TextXmlTest extends ANgoyTest {

	@Component(selector = "test", contentType = "text/xml", template = "<xml><person *ngFor=\"let p of persons\"><name>{{p.name}}</name></person></xml>")
	public static class Cmp {
		public List<Person> persons = asList(new Person("peter", 22), new Person("paul", 26), new Person("marü", 24));
	}

	@Test
	public void test() throws Exception {
		assertThat(render(Cmp.class)).isEqualTo("<xml><person><name>peter</name></person><person><name>paul</name></person><person><name>marü</name></person></xml>");
	}

	//

	@Component(selector = "person", template = "<name>{{person.name}}</name>")
	public static class PersonCmp {
		@Input
		public Person person;
	}

	@Component(selector = "test", contentType = "text/xml", declarations = { PersonCmp.class }, template = "<xml><person *ngFor=\"let p of persons\" [person]=\"p\"></person></xml>")
	public static class CmpWithPersonCmp {
		public List<Person> persons = asList(new Person("peter", 22), new Person("paul", 26), new Person("marü", 24));
	}

	@Test
	public void testWithPersonCmp() throws Exception {
		assertThat(render(CmpWithPersonCmp.class)).isEqualTo("<xml><person><name>peter</name></person><person><name>paul</name></person><person><name>marü</name></person></xml>");
	}
}
