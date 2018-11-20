package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class AttrBindingNullTest extends ANgoyTest {

	@Component(selector = "test", template = "<person [vip]=\"vip\"></person>")
	public static class Person {
		public Boolean vip = null;
	}

	@Test
	public void testAttrNull() {
		assertThat(render(Person.class)).isEqualTo("<person></person>");
	}
}
