package ngoy.xml;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.model.Person;

public class XmlTest extends ANgoyTest {

	@Component(selector = "person", template = "<name>{{person.name}}</name><age>{{person.age}}</age>")
	public static class PersonCmp {
		@Input
		public Person person;
	}

	@Component(selector = "test", contentType = "text/xml", template = "<doc [id]=\"docId\"><person *ngFor=\"let person of persons\" [person]=\"person\"></person></doc>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Cmp {
		public List<Person> persons = asList(new Person("petör", 22), new Person("paul", 26), new Person("märy", 24));

		public String docId = "28900";
	}

	@Test
	public void testXml() throws Exception {
		assertThat(render(Cmp.class))
				.isEqualTo("<doc id=\"28900\"><person><name>petör</name><age>22</age></person><person><name>paul</name><age>26</age></person><person><name>märy</name><age>24</age></person></doc>");
	}
}
