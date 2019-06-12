package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateUrlTest extends ANgoyTest {

	@Component(selector = "person", templateUrl = "template-url-test-person.component.html")
	public static class PersonRelativeCmp {
	}

	@Component(selector = "person-abs", templateUrl = "/ngoy/template-url-test-person-abs.component.html")
	public static class PersonAbsCmp {
	}

	@Component(selector = "test", template = "<person></person><person-abs></person-abs>")
	@NgModule(declarations = { PersonRelativeCmp.class, PersonAbsCmp.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<person>template-url-test-kuckuck</person><person-abs>template-url-test-abs-kuckuck</person-abs>");
	}
}
