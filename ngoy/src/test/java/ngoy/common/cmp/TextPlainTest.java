package ngoy.common.cmp;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.model.Person;

public class TextPlainTest extends ANgoyTest {
	@Component(selector = "test", contentType = "text/plain", template = "\nage of {{person.getName()}}:\t {{person.getAge()}}\n")
	public static class Cmp {
		public Person person = new Person("peter", 22);
	}

	@Test
	public void test() throws Exception {
		assertThat(render(Cmp.class)).isEqualTo("\nage of peter:\t 22\n");
	}

	//

	@Component(selector = "test", contentType = "text/plain", template = "<ng-container *ngFor=\"let p of persons\">{{p.getName()}}{{delim}}{{p.getAge()}}\n</ng-container>")
	public static class CmpRepeated {
		public final String delim = ";";
		public List<Person> persons = asList(new Person("petör", 22), new Person("paul", 26), new Person("märy", 24));
	}

	@Test
	public void testRepeated() throws Exception {
		assertThat(render(CmpRepeated.class)).isEqualTo("petör;22\npaul;26\nmäry;24\n");
	}
}
