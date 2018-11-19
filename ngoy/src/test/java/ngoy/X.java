package ngoy;

public class X {
	public static void render(ngoy.core.internal.Ctx ctx) throws Exception {
		String _$l0;
		ctx.print("<!DOCTYPE html>");
		ctx.printEscaped("\r\n");
		ctx.printEscaped(ctx.eval("_ctx.pipe(\"ngoy.common.UpperCasePipe\").transform('a')"));
	}
}
