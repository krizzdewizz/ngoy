package ngoy;
@SuppressWarnings("all")
public class X {
private static ngoy.core.PipeTransform __$uppercase;  private static Object $uppercase(Object obj, Object... args) {
      return __$uppercase.transform(obj, args);
  }
private static ngoy.core.PipeTransform __$date;  private static Object $date(Object obj, Object... args) {
      return __$date.transform(obj, args);
  }
private static ngoy.core.PipeTransform __$lowercase;  private static Object $lowercase(Object obj, Object... args) {
      return __$lowercase.transform(obj, args);
  }
private static ngoy.core.PipeTransform __$capitalize;  private static Object $capitalize(Object obj, Object... args) {
      return __$capitalize.transform(obj, args);
  }
    public static void render(ngoy.core.internal.Ctx ctx) throws Exception {
    __$uppercase = ctx.getPipe("uppercase");
    __$date = ctx.getPipe("date");
    __$lowercase = ctx.getPipe("lowercase");
    __$capitalize = ctx.getPipe("capitalize");
    String _$l_textOverride_0;
    ngoy.parser.JavaParserTest.Cmp _$l_cmp_1=(ngoy.parser.JavaParserTest.Cmp)ctx.cmpNew(ngoy.parser.JavaParserTest.Cmp.class);
    {
      ctx.cmpInit(_$l_cmp_1);
ctx.print("<pre");
      java.util.List<String> _$l_classlist_2 = new java.util.ArrayList<String>();
      _$l_classlist_2.add("x");
      if (1 == 1) {
        _$l_classlist_2.add("a");
      }
      if (!_$l_classlist_2.isEmpty()) {
ctx.print(" class=\"");
ctx.printEscaped(ctx.join(_$l_classlist_2, " "));
ctx.print("\"");
      }
      java.util.List<String> _$l_stylelist_3 = new java.util.ArrayList<String>();
      _$l_stylelist_3.add("white-space:nowrap");
      _$l_stylelist_3.add("qbert:red");
      Object _$l_expr_4=_$l_cmp_1.appName;
      if (_$l_expr_4 != null) {
_$l_stylelist_3.add("color:".concat(_$l_expr_4.toString())        );
      }
      Object _$l_expr_5=10;
      if (_$l_expr_5 != null) {
_$l_stylelist_3.add("width:".concat(_$l_expr_5.toString()).concat("px")        );
      }
      if (!_$l_stylelist_3.isEmpty()) {
ctx.print(" style=\"");
ctx.printEscaped(ctx.join(_$l_stylelist_3, ";"));
ctx.print("\"");
      }
ctx.print("></pre>");
ctx.printEscaped("app: ");
ctx.printEscaped(_$l_cmp_1.appName);
      for (ngoy.core.internal.IterableWithVariables.Iter _$l_iter_6 = ctx.forOfStart(_$l_cmp_1.getHobbies()).iterator(); _$l_iter_6.hasNext();) {
        java.lang.String hobby = (java.lang.String)_$l_iter_6.next();
        int i=_$l_iter_6.index;
        boolean fir=_$l_iter_6.first;
ctx.print("<b>");
ctx.printEscaped(hobby);
ctx.printEscaped(i);
ctx.printEscaped(fir);
ctx.print("</b>");
      }
      if (_$l_cmp_1.appName.equals("a")) {
        ngoy.parser.JavaParserTest.PersonCmp _$l_cmp_7=(ngoy.parser.JavaParserTest.PersonCmp)ctx.cmpNew(ngoy.parser.JavaParserTest.PersonCmp.class);
        {
          _$l_cmp_7.person=(ngoy.model.Person)_$l_cmp_1.peter;
          _$l_cmp_7.setPerson2(_$l_cmp_1.peter);
          ctx.cmpInit(_$l_cmp_7);
ctx.print("<person ,>");
ctx.printEscaped("hello: ");
ctx.printEscaped($date(_$l_cmp_7.person.getName(), "YYYY"));
          ctx.cmpDestroy(_$l_cmp_7);
        }
ctx.print("</person>");
      }
      ctx.cmpDestroy(_$l_cmp_1);
    }
    }
}
