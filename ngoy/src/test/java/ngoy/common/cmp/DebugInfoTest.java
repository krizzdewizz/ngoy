package ngoy.common.cmp;

import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgoyException;

public class DebugInfoTest extends ANgoyTest {

	@Component(selector = "", templateUrl = "debug-info.component.html")
	public static class TestCmp {
		public String name;
	}

	@Test
	public void test() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Runtime error in"));
		expectedEx.expectMessage(containsString("name.trim()"));
		expectedEx.expectMessage(containsString("java.lang.NullPointerException"));
		expectedEx.expectMessage(containsString("debug-info.component.html"));
		expectedEx.expectMessage(containsString("position: [4:1"));
		render(TestCmp.class);
	}

	//

	@Component(selector = "", template = "this\n" + //
			"is the\n" + //
			"line\n" + //
			"<p>{{name.trim()}}</p>\n" + //
			"a\n" + //
			"b\n" + //
			"c\n" + //
			"")
	public static class TestInlineCmp {
		public String name;
	}

	@Test
	public void testInline() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Runtime error in"));
		expectedEx.expectMessage(containsString("name.trim()"));
		expectedEx.expectMessage(containsString("java.lang.NullPointerException"));
		expectedEx.expectMessage(containsString(TestInlineCmp.class.getName()));
		expectedEx.expectMessage(containsString("position: [4:1"));
		render(TestInlineCmp.class);
	}
}
