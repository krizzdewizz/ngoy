package ngoy;
public class X {
    public static void render(ngoy.core.internal.Ctx ctx) throws Exception {
    String _$l0;
ctx.print("<a");
    Object _$l1= ctx.evalStyles(new String[]{"color", "'color'"},new String[]{"width.px", "'width'"});
    if (_$l1 != null) {
ctx.print(" style=\"");
ctx.printEscaped(_$l1);
ctx.print("\"");
    }
ctx.print("></a>");
    }
}
