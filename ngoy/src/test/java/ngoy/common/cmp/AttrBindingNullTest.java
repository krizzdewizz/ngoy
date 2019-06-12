package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
