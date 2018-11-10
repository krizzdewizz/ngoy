package org.ngoy.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Util.fieldName;

import org.junit.Test;

public class UtilTest {

	@Test
	public void test() {
		assertThat(fieldName("setName")).isEqualTo("name");
		assertThat(fieldName("name")).isEqualTo("name");
		assertThat(fieldName("set")).isEqualTo("set");
	}
}
