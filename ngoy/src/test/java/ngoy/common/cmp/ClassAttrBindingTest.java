package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class ClassAttrBindingTest extends ANgoyTest {
	@Component(selector = "test", template = "<a class=\"a\" [class.x]=\"1==1\"></a>")
	public static class ClassAttr {
	}

	@Test
	public void testClassAttr() {
		assertThat(render(ClassAttr.class)).isEqualTo("<a class=\"a x\"></a>");
	}

	//

	@Component(selector = "test", template = "<a [class.x]=\"1==1\" class=\"a\"></a>")
	public static class ClassAttrOrderIrrelevant {
	}

	@Test
	public void testClassAttrOrderIrrelevant() {
		assertThat(render(ClassAttrOrderIrrelevant.class)).isEqualTo("<a class=\"a x\"></a>");
	}

	//

	@Component(selector = "test", template = "<a [ngClass]=\"{q:true, a: false, 'a-a': 'x'=='x'}\"></a>")
	public static class ClassAttrObj {
	}

	@Test
	public void testClassAttrObj() {
		assertThat(render(ClassAttrObj.class)).isEqualTo("<a class=\"q a-a\"></a>");
	}

	//

	@Component(selector = "test", template = "<a [class.x]=\"false\" [ngClass]=\"{z:false}\"></a>")
	public static class ClassAttrNoClass {
	}

	@Test
	public void testClassAttrNull() {
		assertThat(render(ClassAttrNoClass.class)).isEqualTo("<a></a>");
	}

}
