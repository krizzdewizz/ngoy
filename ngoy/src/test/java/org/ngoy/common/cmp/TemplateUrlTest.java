package org.ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;

public class TemplateUrlTest extends ANgoyTest {

	@Component(selector = "person", templateUrl = "template-url-test-person.component.html")
	public static class PersonRelativeCmp {
	}
	
	@Component(selector = "person-abs", templateUrl = "/org/ngoy/template-url-test-person-abs.component.html")
	public static class PersonAbsCmp {
	}


	@Component(selector = "test", declarations = {
			PersonRelativeCmp.class, PersonAbsCmp.class }, template = "<person></person><person-abs></person-abs>")
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<person>template-url-test-kuckuck</person><person-abs>template-url-test-abs-kuckuck</person-abs>");
	}
}
