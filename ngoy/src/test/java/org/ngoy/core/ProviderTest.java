package org.ngoy.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Provider.of;
import static org.ngoy.core.Provider.useClass;
import static org.ngoy.core.Provider.useValue;

import org.junit.Test;

public class ProviderTest {

	@Test
	public void testOf() {
		Provider[] ps = of(String.class, Integer.class);
		assertThat(ps).hasSize(2);

		assertThat(ps[0].getProvide()).isEqualTo(String.class);
		assertThat(ps[0].getUseClass()).isEqualTo(String.class);
		assertThat(ps[0].getUseValue()).isNull();
		assertThat(ps[0].toString()).contains("java.lang.String -> useClass: java.lang.String");

		assertThat(ps[1].getProvide()).isEqualTo(Integer.class);
		assertThat(ps[1].getUseClass()).isEqualTo(Integer.class);
		assertThat(ps[1].getUseValue()).isNull();
		assertThat(ps[1].toString()).contains("java.lang.Integer -> useClass: java.lang.Integer");
	}

	@Test
	public void testUseClass() {
		Provider p = useClass(Number.class, Integer.class);

		assertThat(p.getProvide()).isEqualTo(Number.class);
		assertThat(p.getUseClass()).isEqualTo(Integer.class);
		assertThat(p.getUseValue()).isNull();
		assertThat(p.toString()).contains("java.lang.Number -> useClass: java.lang.Integer");
	}

	@Test
	public void testUseClassNull() {
		Provider p = useClass(Number.class, null);

		assertThat(p.getProvide()).isEqualTo(Number.class);
		assertThat(p.getUseClass()).isEqualTo(Number.class);
		assertThat(p.getUseValue()).isNull();
		assertThat(p.toString()).contains("java.lang.Number -> useClass: java.lang.Number");
	}

	@Test
	public void testUseValue() {
		Provider p = useValue(Number.class, 99);

		assertThat(p.getProvide()).isEqualTo(Number.class);
		assertThat(p.getUseClass()).isNull();
		assertThat(p.getUseValue()).isEqualTo(99);
		assertThat(p.toString()).contains("java.lang.Number -> useValue: java.lang.Integer");
	}
}
