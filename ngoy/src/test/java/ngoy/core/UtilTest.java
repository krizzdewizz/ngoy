package ngoy.core;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

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
}
