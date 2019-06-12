package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.core.Provide;
import ngoy.internal.scan.ClassScannerTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModuleTest extends ANgoyTest {

	@Component(selector = "a", template = "aa")
	public static class ACmp {

	}

	public static class AService {
		public String getValue() {
			return "avala";
		}
	}

	@NgModule(declarations = { ACmp.class }, providers = { AService.class })
	public static class ModuleA {
	}

	@Component(selector = "test", template = "<a [value]=\"aService.getValue()\" ></a>")
	@NgModule(imports = { ModuleA.class })
	public static class Cmp {
		@Inject
		public AService aService;
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<a value=\"avala\">aa</a>");
	}

	//

	public static class BService extends AService {
		@Override
		public String getValue() {
			return "bvalb";
		}
	}

	@Component(selector = "test", template = "<a [value]=\"aService.getValue()\" ></a>")
	@NgModule(imports = { ModuleA.class }, provide = @Provide(provide = AService.class, useClass = BService.class))
	public static class BCmp {
		@Inject
		public AService aService;
	}

	@Test
	public void testB() {
		assertThat(render(BCmp.class)).isEqualTo("<a value=\"bvalb\">aa</a>");
	}

	//

	public static class CService extends AService {
		@Override
		public String getValue() {
			return "cvalc";
		}
	}

	@Component(selector = "test", template = "<a [value]=\"aService.getValue()\" ></a>", provide = @Provide(provide = AService.class, useClass = CService.class))
	@NgModule(imports = { ModuleA.class })
	public static class CCmp {
		@Inject
		public AService aService;
	}

	@Test
	public void testC() {
		assertThat(render(CCmp.class)).isEqualTo("<a value=\"cvalc\">aa</a>");
	}

	//

	@Component(selector = "test", template = "<a></a><b></b>")
	public static class PackPrefix {
	}

	@Test
	public void testPackagePrefix() {
		assertThat(render(PackPrefix.class, builder -> builder.modules(ClassScannerTest.class.getPackage()))).isEqualTo("<a>scana</a><b>scanb</b>");
	}
}
