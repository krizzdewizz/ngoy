package ngoy.core.internal;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericTypeWrongTest {

	public static class AClass {
		public Set<Map.Entry<String, Object>> formInputs;
	}

	// fails with --all-tests, ok when run only this test
//	@org.junit.Test
	public void testWrongTypeName() throws Exception {
		Type type = AClass.class.getField("formInputs")
				.getGenericType();

		assertThat(type.toString()).isEqualTo("java.util.Set<java.util.Map.java.util.Map$Entry<java.lang.String, java.lang.Object>>");
	}
}
