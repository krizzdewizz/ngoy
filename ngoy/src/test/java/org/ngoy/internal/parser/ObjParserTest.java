package org.ngoy.internal.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

public class ObjParserTest {
	@Test
	public void test() throws Exception {
		Map<String, String> map = ObjParser.parse("{ blob: true(), 'qbert': it.name == 'abc'  }");
		System.out.println(map);
		assertThat(map.size()).isEqualTo(2);
		assertThat(map.get("blob")).isEqualTo("true()");
	}
}
