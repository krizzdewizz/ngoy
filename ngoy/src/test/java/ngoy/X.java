package ngoy;

@SuppressWarnings("all")
public class X {
	public static void render(ngoy.core.internal.Ctx ctx) throws Exception {
		String _$l0;
		ctx.pushCmpContextInput("ngoy.testapp.PersonDetailComponent", "00person", "null");
		ctx.pushCmpContext("ngoy.testapp.PersonDetailComponent");
		ctx.print("<person>");
		ctx.printEscaped("NAME: ");
		ctx.printEscaped(ctx.eval("person.name"));
		ctx.pushParentContext();
		ctx.popContext();
		ctx.printEscaped("\r\n");
		ctx.popCmpContext();
		ctx.print("</person>");
	}
}
