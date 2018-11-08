package org.ngoy;
public class X {
    public static void render(org.ngoy.core.internal.Ctx ctx) throws Exception {
ctx.print("<html><head></head><body>");
    for (Object _$$l0: ctx.evalIterable("persons")) {
        ctx.pushContext("p", _$$l0);
ctx.print("<ng-container>");
ctx.printEscaped(ctx.eval("p.name"));
ctx.print("</ng-container>");
      ctx.popContext();
    }
ctx.print("</body></html>");
    }
}
