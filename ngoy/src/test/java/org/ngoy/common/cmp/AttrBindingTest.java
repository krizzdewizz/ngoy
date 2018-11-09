package org.ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;

public class AttrBindingTest extends ANgoyTest {

	@Component(selector = "test", template = "<a [beer]=\"'free'\" [abc]=\"null\" [href]=\"'world'\" title=\"{{'x'}}x{{false}}\"></a>")
	public static class Attr {
	}

	@Test
	public void testAttr() {
		assertThat(render(Attr.class)).isEqualTo("<a beer=\"free\" href=\"world\" title=\"xxfalse\"></a>");
	}
}
