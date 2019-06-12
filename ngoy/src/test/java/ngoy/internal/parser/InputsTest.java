package ngoy.internal.parser;

import ngoy.ANgoyTest;
import org.junit.Test;

import static ngoy.internal.parser.Inputs.fieldName;
import static org.assertj.core.api.Assertions.assertThat;

public class InputsTest extends ANgoyTest {

	@Test
	public void test() {
		assertThat(fieldName("setName")).isEqualTo("name");
		assertThat(fieldName("name")).isEqualTo("name");
		assertThat(fieldName("set")).isEqualTo("set");
	}
}
