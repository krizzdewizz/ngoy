package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.model.Person;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TextXmlTest extends ANgoyTest {

	@Component(selector = "test", contentType = "text/xml", template = "<xml><person *ngFor=\"let p of persons\"><name>{{p.getName()}}</name></person></xml>")
	public static class Cmp {
		public List<Person> persons = asList(new Person("peter", 22), new Person("paul", 26), new Person("marü", 24));
	}

	@Test
	public void test() throws Exception {
		assertThat(render(Cmp.class)).isEqualTo("<xml><person><name>peter</name></person><person><name>paul</name></person><person><name>marü</name></person></xml>");
	}

	//

	@Component(selector = "person", template = "<name>{{person.getName()}}</name>")
	public static class PersonCmp {
		@Input
		public Person person;
	}

	@Component(selector = "test", contentType = "text/xml", template = "<xml><person *ngFor=\"let p of persons\" [person]=\"p\"></person></xml>")
	@NgModule(declarations = { PersonCmp.class })
	public static class CmpWithPersonCmp {
		public List<Person> persons = asList(new Person("peter", 22), new Person("paul", 26), new Person("marü", 24));
	}

	@Test
	public void testWithPersonCmp() throws Exception {
		assertThat(render(CmpWithPersonCmp.class)).isEqualTo("<xml><person><name>peter</name></person><person><name>paul</name></person><person><name>marü</name></person></xml>");
	}
}
