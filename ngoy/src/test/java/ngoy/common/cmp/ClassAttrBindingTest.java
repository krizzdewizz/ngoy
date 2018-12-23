package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class ClassAttrBindingTest extends ANgoyTest {

	@Component(selector = "", template = "<a class=\"a\" [class.x]=\"1==1\"></a>")
	public static class ClassAttrCmp {
	}

	@Test
	public void testClassAttr() {
		assertThat(render(ClassAttrCmp.class)).isEqualTo("<a class=\"a x\"></a>");
	}

	//

	@Component(selector = "", template = "<a [class.x]=\"1==1\" class=\"a\"></a>")
	public static class OrderIrrelevantCmp {
	}

	@Test
	public void testClassAttrOrderIrrelevant() {
		assertThat(render(OrderIrrelevantCmp.class)).isEqualTo("<a class=\"a x\"></a>");
	}

	//

	@Component(selector = "", template = "<a [ngClass]=\"all\"></a>")
	public static class ObjCmp {
		public Map<String, Boolean> all = new HashMap<>();

		public ObjCmp() {
			all.put("q", true);
			all.put("a", false);
			all.put("a-a", true);
		}
	}

	@Test
	public void testClassAttrObj() {
		assertThat(render(ObjCmp.class)).isEqualTo("<a class=\"q a-a\"></a>");
	}

	//

	@Component(selector = "", template = "<a [ngClass]=\"Map('q', true, 'a', false, 'a-a', true)\"></a>")
	public static class ObjLiteralCmp {
		public Map<String, Boolean> all = new HashMap<>();

		public ObjLiteralCmp() {
			all.put("q", true);
			all.put("a", false);
			all.put("a-a", true);
		}
	}

	@Test
	public void testClassAttrObjMapFun() {
		assertThat(render(ObjLiteralCmp.class)).isEqualTo("<a class=\"q a-a\"></a>");
	}

	//

	@Component(selector = "", template = "<a [class.x]=\"false\" [ngClass]=\"all\"></a>")
	public static class NullCmp {
		public Map<String, Boolean> all = new HashMap<>();
	}

	@Test
	public void testClassAttrNull() {
		assertThat(render(NullCmp.class)).isEqualTo("<a></a>");
	}

}
