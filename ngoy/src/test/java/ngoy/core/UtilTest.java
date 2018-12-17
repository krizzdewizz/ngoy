package ngoy.core;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UtilTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void getTemplate_noAnnotation() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Annotation"));
		expectedEx.expectMessage(containsString(format("not found on class %s", UtilTest.class.getName())));
		Util.getTemplate(UtilTest.class);
	}

	@Component(selector = "", templateUrl = "does_not_exist")
	public static class NotFoundCmp {
	}

	@Test
	public void getTemplate_notFound() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Template could not be found"));
		expectedEx.expectMessage(containsString("does_not_exist"));
		Util.getTemplate(NotFoundCmp.class);
	}

	@Component(selector = "", templateUrl = "test-template.html")
	public static class OkCmp {
	}

	@Test
	public void getTemplate_ok() {
		assertThat(Util.getTemplate(OkCmp.class)).isEqualTo("test-abc");
	}

	@Test
	public void primitiveToRefType() {
		assertThat(Util.primitiveToRefType(boolean.class)).isEqualTo("Boolean");
		assertThat(Util.primitiveToRefType(byte.class)).isEqualTo("Byte");
		assertThat(Util.primitiveToRefType(char.class)).isEqualTo("Character");
		assertThat(Util.primitiveToRefType(short.class)).isEqualTo("Short");
		assertThat(Util.primitiveToRefType(int.class)).isEqualTo("Integer");
		assertThat(Util.primitiveToRefType(long.class)).isEqualTo("Long");
		assertThat(Util.primitiveToRefType(float.class)).isEqualTo("Float");
		assertThat(Util.primitiveToRefType(String.class)).isEqualTo(String.class.getName());
		assertThat(Util.primitiveToRefType(Locale.class)).isEqualTo(Locale.class.getName());
	}
}
