package ngoy.util;

import static ngoy.core.Util.fieldName;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UtilTest {

	@Test
	public void test() {
		assertThat(fieldName("setName")).isEqualTo("name");
		assertThat(fieldName("name")).isEqualTo("name");
		assertThat(fieldName("set")).isEqualTo("set");
	}
}
