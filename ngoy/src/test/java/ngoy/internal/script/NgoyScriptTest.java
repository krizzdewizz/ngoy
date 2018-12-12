package ngoy.internal.script;

public class NgoyScriptTest {

//	@Rule
//	public ExpectedException expectedEx = ExpectedException.none();
//
//	@Test
//	public void test() {
//		Ctx ctx = Ctx.of()
//				.variable("x", "peter");
//		assertThat(run("let q = x\nreturn x", ctx)).isEqualTo("peter");
//	}
//
//	@Test
//	public void testIff() {
//		Ctx ctx = Ctx.of(new Global())
//				.variable("x", "peter");
//		assertThat(run("iif(x == 'peter', 'a', 'b')", ctx)).isEqualTo("a");
//	}
//
//	@Test
//	public void testSplit() {
//		Ctx ctx = Ctx.of()
//				.variable("$", "peter");
//		assertThat(run("let q = $.toUpperCase() \n let n = q.length()\nq + ': ' + n", ctx)).isEqualTo("PETER: 5");
//	}
//
//	@Test
//	public void testErrorStatement() {
//		expectedEx.expect(NgoyException.class);
//		expectedEx.expectMessage(containsString("Cannot read property 'a' of null"));
//		run("let q = 'x'\na", Ctx.of());
//	}
//
//	@Test
//	public void testVarAlreadyDefined() {
//		expectedEx.expect(NgoyException.class);
//		expectedEx.expectMessage(containsString("line 4"));
//		expectedEx.expectMessage(containsString("variable 'q' is already defined"));
//		run("let q = 'x'\n\n\nlet q = 'a'", Ctx.of());
//	}
//
//	private Object run(String script, Ctx ctx) {
//		return new NgoyScript(null).run(script, ctx);
//	}
}
