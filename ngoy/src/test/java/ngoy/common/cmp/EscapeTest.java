package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class EscapeTest extends ANgoyTest {

	@Component(selector = "test", template = "<div title=\"{{forbiddenChars}}\"> &lt;&gt; &quot; &amp; {{forbiddenChars}}</div>")
	public static class Cmp {
		public String forbiddenChars = "<>\"&";
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<div title=\"&lt;&gt;&quot;&amp;\"> &lt;&gt; &quot; &amp; &lt;&gt;&quot;&amp;</div>");
	}

	//

	@Component(selector = "test", template = "<div title=\"x\" [q]=\"forbiddenChars\" > &lt;&gt; \" &</div>")
	public static class Cmp2 {
		public String forbiddenChars = "<>\"&";
	}

	@Test
	public void test2() {
		assertThat(render(Cmp2.class)).isEqualTo("<div title=\"x\" q=\"&lt;&gt;&quot;&amp;\"> &lt;&gt; &quot; &amp;</div>");
	}

	//

	@Test
	public void testScriptOrStyle() {
		assertThat(render("<a> > </a> <script> var x = 0 > 1, y = 0 &gt; 1; </script><body><style> a > .active { color: red }</style></body>")).isEqualTo("<a> &gt; </a> <script> var x = 0 > 1, y = 0 &gt; 1; </script><body><style> a > .active { color: red }</style></body>");
	}
}
