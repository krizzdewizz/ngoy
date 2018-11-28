package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;

public class ClassAttrBindingTest extends ANgoyTest {
	@Test
	public void testClassAttr() {
		assertThat(render("<a class=\"a\" [class.x]=\"1==1\"></a>")).isEqualTo("<a class=\"a x\"></a>");
	}

	@Test
	public void testClassAttrOrderIrrelevant() {
		assertThat(render("<a [class.x]=\"1==1\" class=\"a\"></a>")).isEqualTo("<a class=\"a x\"></a>");
	}

	@Test
	public void testClassAttrObj() {
		assertThat(render("<a [ngClass]=\"{q:true, a: false, 'a-a': 'x'=='x'}\"></a>")).isEqualTo("<a class=\"q a-a\"></a>");
	}

	@Test
	public void testClassAttrNull() {
		assertThat(render("<a [class.x]=\"false\" [ngClass]=\"{z:false}\"></a>")).isEqualTo("<a></a>");
	}

}
