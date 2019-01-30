package ngoy.core.internal;

public class XTemplate {
	public static ngoy.core.internal.TemplateRender createRenderer(ngoy.core.Injector injector) {
		return new Renderer(injector);
	}

	private static <K, V> java.util.Map<K, V> Map(Object... pairs) {
		return ngoy.core.internal.Ctx.<K, V>Map(pairs);
	}

	private static <T> java.util.List List(T... items) {
		return ngoy.core.internal.Ctx.<T>List(items);
	}

	private static <T> java.util.Set Set(T... items) {
		return ngoy.core.internal.Ctx.<T>Set(items);
	}

	private static final String[] __strings = new String[] { "<person>", "hello: ", "</person>" };

	private static class Renderer implements ngoy.core.internal.TemplateRender {

		private static void __renderngoycommoncmpAttrBindingTest$PersonNonStringParamCmp(final Void noparent, final ngoy.common.cmp.AttrBindingTest.AttrNonStringParam __AttrNonStringParam,
				final ngoy.common.cmp.AttrBindingTest.PersonNonStringParamCmp __PersonNonStringParamCmp, final ngoy.core.internal.Ctx __, final String[] __lastExpr, String[] __textOverride) {
			__.p(__strings[0]);
			__.p(__strings[1]);
			__lastExpr[0] = "component: ngoy.common.cmp.AttrBindingTest$PersonNonStringParamCmp, position: [1:1 @0]:::title";
			// EXPR:::component: ngoy.common.cmp.AttrBindingTest$PersonNonStringParamCmp,
			// position: [1:1 @0]:::title
			__.pe(__PersonNonStringParamCmp.title);
			__lastExpr[0] = "component: ngoy.common.cmp.AttrBindingTest$PersonNonStringParamCmp, position: [1:1 @0]:::title2";
			// EXPR:::component: ngoy.common.cmp.AttrBindingTest$PersonNonStringParamCmp,
			// position: [1:1 @0]:::title2
			__.pe(__PersonNonStringParamCmp.title2);
			__lastExpr[0] = "component: ngoy.common.cmp.AttrBindingTest$PersonNonStringParamCmp, position: [1:1 @0]:::title3";
			// EXPR:::component: ngoy.common.cmp.AttrBindingTest$PersonNonStringParamCmp,
			// position: [1:1 @0]:::title3
			__.pe(__PersonNonStringParamCmp.title3);
			__lastExpr[0] = "component: ngoy.common.cmp.AttrBindingTest$PersonNonStringParamCmp, position: [1:1 @0]:::title4";
			// EXPR:::component: ngoy.common.cmp.AttrBindingTest$PersonNonStringParamCmp,
			// position: [1:1 @0]:::title4
			__.pe(__PersonNonStringParamCmp.title4);
		}

		private static void __renderngoycommoncmpAttrBindingTest$AttrNonStringParam(final Void noparent, final ngoy.common.cmp.AttrBindingTest.AttrNonStringParam __appRoot,
				final ngoy.common.cmp.AttrBindingTest.AttrNonStringParam __AttrNonStringParam, final ngoy.core.internal.Ctx __, final String[] __lastExpr, String[] __textOverride) {
			final ngoy.common.cmp.AttrBindingTest.PersonNonStringParamCmp __PersonNonStringParamCmp = (ngoy.common.cmp.AttrBindingTest.PersonNonStringParamCmp) __
					.cmpNew(ngoy.common.cmp.AttrBindingTest.PersonNonStringParamCmp.class);
			{
				__lastExpr[0] = "component: ngoy.common.cmp.AttrBindingTest$AttrNonStringParam, position: [1:1 @0]:::0";
				// EXPR:::component: ngoy.common.cmp.AttrBindingTest$AttrNonStringParam,
				// position: [1:1 @0]:::0
				__PersonNonStringParamCmp.setNumber("0");
				__renderngoycommoncmpAttrBindingTest$PersonNonStringParamCmp(null, __AttrNonStringParam, __PersonNonStringParamCmp, __, __lastExpr, __textOverride);
			}
			__.p(__strings[2]);
		}

		private Renderer(ngoy.core.Injector injector) {

		}

		public void render(ngoy.core.internal.Ctx __) throws ngoy.core.internal.RenderException {
			String[] __lastExpr = new String[] { "" };
			try {
				String[] __textOverride = new String[1];
				final ngoy.common.cmp.AttrBindingTest.AttrNonStringParam __AttrNonStringParam = (ngoy.common.cmp.AttrBindingTest.AttrNonStringParam) __.getCmpInstance();
				{
					__renderngoycommoncmpAttrBindingTest$AttrNonStringParam(null, __AttrNonStringParam, __AttrNonStringParam, __, __lastExpr, __textOverride);
				}
			} catch (Exception __e) {
				throw new ngoy.core.internal.RenderException(__e, __lastExpr[0]);
			}
		}
	}
}
