package ngoy.common.cmp;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.model.Person;

public class TextPlainWithCmpsTest extends ANgoyTest {

	@Component(selector = "header", template = "Welcome, {{name}}\n")
	public static class HeaderCmp {
		@Input
		public String name;
	}

	@Component(selector = "footer", template = "bye")
	public static class FooterCmp {
	}

	@Component(selector = "test", contentType = "text/plain", template = "<header [name]=\"person.name\"></header>\nage of {{person.name}}:\t {{person.age}}\nhobbies:\t\n<span *ngFor=\"let h of hobbies\">{{h}}\n</span><footer></footer>")
	@NgModule(declarations = { HeaderCmp.class, FooterCmp.class })
	public static class Cmp {
		public Person person = new Person("peter", 22);
		public List<String> hobbies = asList("music", "surfing", "dancing");
	}

	@Test
	public void test() throws Exception {
		assertThat(render(Cmp.class)).isEqualTo("Welcome, peter\n" + //
				"\n" + //
				"age of peter:	 22\n" + //
				"hobbies:	\n" + //
				"music\n" + //
				"surfing\n" + //
				"dancing\n" + //
				"bye");
	}
}
