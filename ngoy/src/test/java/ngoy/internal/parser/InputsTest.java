package ngoy.internal.parser;

import static ngoy.internal.parser.Inputs.fieldName;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;

public class InputsTest extends ANgoyTest {

	@Test
	public void test() {
		assertThat(fieldName("setName")).isEqualTo("name");
		assertThat(fieldName("name")).isEqualTo("name");
		assertThat(fieldName("set")).isEqualTo("set");
	}
}
