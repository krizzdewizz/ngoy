package ngoy.core;

import static java.lang.String.format;
import static ngoy.core.Util.primitiveToRefType;
import static ngoy.core.Util.sourceClassName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ngoy.core.UtilTest.Inner.Sub$Inner;
import ngoy.core.UtilTest.Inner.SubInner;
import ngoy.model.Person;

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
	public void testPrimitiveToRefType() {
		assertThat(primitiveToRefType(boolean.class.getName())).isEqualTo("Boolean");
		assertThat(primitiveToRefType(byte.class.getName())).isEqualTo("Byte");
		assertThat(primitiveToRefType(char.class.getName())).isEqualTo("Character");
		assertThat(primitiveToRefType(short.class.getName())).isEqualTo("Short");
		assertThat(primitiveToRefType(int.class.getName())).isEqualTo("Integer");
		assertThat(primitiveToRefType(long.class.getName())).isEqualTo("Long");
		assertThat(primitiveToRefType(float.class.getName())).isEqualTo("Float");
		assertThat(primitiveToRefType(String.class.getName())).isEqualTo(String.class.getName());
		assertThat(primitiveToRefType(Locale.class.getName())).isEqualTo(Locale.class.getName());
	}

	public static class Inner {
		public class SubInner {
		}

		public class Sub$Inner {
		}
	}

	@Test
	public void testSourceClassName() {
		assertThat(sourceClassName(String.class)).isEqualTo("java.lang.String");
		assertThat(sourceClassName(Inner.class)).isEqualTo("ngoy.core.UtilTest.Inner");
		assertThat(sourceClassName(SubInner.class)).isEqualTo("ngoy.core.UtilTest.Inner.SubInner");
		assertThat(sourceClassName(Sub$Inner.class)).isEqualTo("ngoy.core.UtilTest.Inner.Sub$Inner");
	}

	private interface TestItf {
		Object getObj();

		Object[] getObjArray();

		Object[][] getObjArrayArray();

		List<String> getList();

		List<String[]> getListArray();

		Map<String[], Person> getMap();
	}

	private Class<?> getReturnType(String meth) throws Exception {
		return TestItf.class.getMethod(meth)
				.getReturnType();
	}

	@Test
	public void testArrays() throws Exception {
		assertThat(sourceClassName(getReturnType("getObj"))).isEqualTo("java.lang.Object");
		assertThat(sourceClassName(getReturnType("getObjArray"))).isEqualTo("java.lang.Object[]");
		assertThat(sourceClassName(getReturnType("getObjArrayArray"))).isEqualTo("java.lang.Object[][]");
	}

	@Test
	public void testGenerics() throws Exception {
		assertThat(sourceClassName(TestItf.class.getMethod("getList"))).isEqualTo("java.util.List<java.lang.String>");
		assertThat(sourceClassName(TestItf.class.getMethod("getListArray"))).isEqualTo("java.util.List<java.lang.String[]>");
		assertThat(sourceClassName(TestItf.class.getMethod("getMap"))).isEqualTo("java.util.Map<java.lang.String[], ngoy.model.Person>");
	}
}
