package org.ngoy;
public class X {
    public static void render(org.ngoy.core.internal.Ctx ctx) throws Exception {
ctx.print("<html><head></head><body>");
    String []_$l0 = new String[10];
    _$l0[0] = "index";
    _$l0[1] = "i";
    _$l0[2] = "first";
    _$l0[3] = "f";
    _$l0[4] = "last";
    _$l0[5] = "l";
    _$l0[6] = "even";
    _$l0[7] = "e";
    _$l0[8] = "odd";
    _$l0[9] = "o";
    for (Object _$l1: ctx.forOfStart("persons", _$l0)) {
        ctx.pushForOfContext("p", _$l1);
ctx.print("<ng-container>");
ctx.printEscaped(ctx.eval("p.name"));
ctx.print("</ng-container>");
      ctx.popContext();
    }
    ctx.forOfEnd();
ctx.print("</body></html>");
    }
}
