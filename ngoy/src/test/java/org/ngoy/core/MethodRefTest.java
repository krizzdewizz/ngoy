package org.ngoy.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;

import org.junit.Test;

public class MethodRefTest {

	private void setDeleted(String unused) {
	}

	@Test
	public void testRefNotSame() {
		Consumer<String> c1 = this::setDeleted;
		Consumer<String> c2 = this::setDeleted;
		assertThat(c1).isNotSameAs(c2);
		assertThat(c1).isNotEqualTo(c2);
	}
}
