package ngoy.internal.scan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.core.ModuleWithProviders;
import ngoy.internal.scan.a.ACmp;
import ngoy.internal.scan.a.ADirective;
import ngoy.internal.scan.a.XCmp;
import ngoy.internal.scan.a.b.BCmp;
import ngoy.internal.scan.a.b.BPipe;
import ngoy.internal.scan.a.b.BService;

public class ClassScannerTest {

	@Test
	public void test() {
		ClassScanner scanner = new ClassScanner();
		ModuleWithProviders<?> mod = scanner.scan(ClassScannerTest.class.getPackage()
				.getName());

		assertThat(mod.getDeclarations()).hasSize(5);
		assertThat(mod.getDeclarations()).contains(ACmp.class, ADirective.class, XCmp.class, BPipe.class, BCmp.class);
		assertThat(mod.getProviders()).hasSize(1);
		assertThat(mod.getProviders()
				.get(0)
				.getProvide()).isEqualTo(BService.class);
	}

	@Test
	public void testExclude() {
		ClassScanner scanner = new ClassScanner().exclude(XCmp.class.getName());
		ModuleWithProviders<?> mod = scanner.scan(ClassScannerTest.class.getPackage()
				.getName());

		assertThat(mod.getDeclarations()).hasSize(4);
		assertThat(mod.getDeclarations()).contains(ACmp.class, ADirective.class, BPipe.class, BCmp.class);
	}
}
