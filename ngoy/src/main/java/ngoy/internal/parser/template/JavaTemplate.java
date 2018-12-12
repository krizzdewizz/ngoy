package ngoy.internal.parser.template;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.unbescape.java.JavaEscape;

import ngoy.core.NgoyException;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;
import ngoy.core.Util;
import ngoy.core.Variable;
import ngoy.core.internal.CmpRef;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.IterableWithVariables;
import ngoy.internal.parser.ExprParser;
import ngoy.internal.parser.ForOfVariable;
import ngoy.internal.parser.Inputs.CmpInput;
import ngoy.internal.parser.Inputs.InputType;
import ngoy.internal.parser.Inputs.ValueType;
import ngoy.internal.parser.ParserHandler;

public class JavaTemplate extends CodeBuilder implements ParserHandler {

	public static String escapeJava(String text) {
		return JavaEscape.escapeJava(text);
	}

	private static class CmpVar {
		private String name;
		private Class<?> cmpClass;

		public CmpVar(String name, Class<?> cmpClass) {
			this.name = name;
			this.cmpClass = cmpClass;
		}
	}

	private final TextOutput out;
	private String textOverrideVar;
	private boolean hadTextOverride;
	private final LinkedList<String> switchVars = new LinkedList<>();
	private final LinkedList<Boolean> switchHadElseIf = new LinkedList<>();
	private final LinkedList<CmpVar> cmpVars = new LinkedList<>();
	private final LinkedList<Set<String>> prefixExcludes = new LinkedList<>();
	private final Set<String> pipeNames = new HashSet<>();
	private final Map<String, Integer> localVars = new HashMap<>();
	private final Map<String, Variable<?>> variables;
	private final boolean bodyOnly;
	private final String contentType;
	private String cmpVar;

	public JavaTemplate(PrintStream prn, String contentType, boolean bodyOnly, Map<String, Variable<?>> variables) {
		super(new PrintStreamPrinter(prn));
		this.bodyOnly = bodyOnly;
		this.variables = variables;
		this.contentType = contentType;
		out = new TextOutput(printer, () -> depth);
	}

	@Override
	public void documentStart(List<Class<?>> pipes) {
		if (!bodyOnly) {
			$("package ngoy;");
			$("@SuppressWarnings(\"all\")");
			$("public class X {");
		}

		addApiHelpers();
		addPipeMethods(pipes);

		$("public static void render(", Ctx.class, " ctx) throws Exception {");
		setPipes(pipes);
		addVariables();

		textOverrideVar = createLocalVar("textOverride");
		$("String ", textOverrideVar, ";");
	}

	private void addApiHelpers() {
		$("private static ", Map.class, " Map(Object...pairs) {");
		$(" return ", Ctx.class, ".Map(pairs);");
		$("}");

		$("private static ", List.class, " List(Object...items) {");
		$(" return ", Ctx.class, ".List(items);");
		$("}");
	}

	private void addVariables() {
		for (Entry<String, Variable<?>> vars : variables.entrySet()) {
			Variable<?> var = vars.getValue();
			String name = vars.getKey();
			$(var.type, " ", name, "=(", var.type, ")ctx.getVariableValue(\"", name, "\");");
		}
	}

	private void addPipeMethods(List<Class<?>> pipes) {
		for (Class<?> pipe : pipes) {
			String pipeName = pipe.getAnnotation(Pipe.class)
					.value();
			String pipeFun = format("$%s", pipeName);
			pipeNames.add(pipeFun);
			$("private static ", PipeTransform.class, " _", pipeFun, ";");
			$("private static Object ", pipeFun, "(Object obj, Object... args) {");
			$("  return _", pipeFun, ".transform(obj, args);");
			$("}");
		}
	}

	private void setPipes(List<Class<?>> pipes) {
		for (Class<?> pipe : pipes) {
			String pipeName = pipe.getAnnotation(Pipe.class)
					.value();
			String pipeFun = format("$%s", pipeName);
			$("_", pipeFun, " = ctx.getPipe(\"", pipeName, "\");");
		}
	}

	@Override
	public void documentEnd() {
		flushOut();
		$("  }"); // render method

		if (!bodyOnly) {
			$("}");
		}
	}

	@Override
	public void text(String text, boolean textIsExpr, boolean escape) {
		if (text.isEmpty()) {
			return;
		}
		if (textIsExpr) {
			printOutExpr(prefixName(text, cmpVars.peek().name));
		} else {
			if (escape) {
				out.print(text, false, true, contentType);
			} else {
				printOut(text);
			}
		}
	}

	private String prefixName(String expr, String prefix) {
		Set<String> ex = new HashSet<>(pipeNames);
		ex.addAll(variables.keySet());
		ex.add("java");
		ex.add("Map");
		ex.add("List");
		Set<String> more = prefixExcludes.peek();
		if (more != null) {
			ex.addAll(more);
		}
		return ExprParser.prefixName(expr, prefix, ex);
	}

	@Override
	protected void doCreate() {
	}

	@Override
	public void elementHead(String name) {
		printOut("<", name);
	}

	@Override
	public void textOverride(String expr) {
		$(textOverrideVar, "=(String)", prefixName(expr, cmpVars.peek().name), ";");
		hadTextOverride = true;
	}

	@Override
	public void elementHeadEnd() {
		printOut(">");

		if (hadTextOverride) {
			flushOut();

			out.printEscaped(textOverrideVar, true);
			$(textOverrideVar, "=null;");

			hadTextOverride = false;
		}
	}

	@Override
	public void attributeClasses(List<String[]> classExprPairs) {
		attributeEval("class", false, classExprPairs);
	}

	@Override
	public void attributeStyles(List<String[]> styleExprPairs) {
		attributeEval("style", true, styleExprPairs);
	}

	@Override
	public void attributeStart(String name, boolean hasValue) {
		printOut(" ", name);
		if (hasValue) {
			printOut("=\"");
		}
	}

	@Override
	public void attributeExpr(String name, String expr) {
		flushOut();
		String evalResultVar = createLocalVar("attrExpr");
		$("Object ", evalResultVar, ";");

		$(evalResultVar, " = ", prefixName(expr, cmpVars.peek().name), ";");

		$("if (", evalResultVar, " != null) {");
		printOut(" ", name, "=\"");
		flushOut();
		out.printEscaped(evalResultVar, true);
		printOut("\"");
		flushOut();
		$("}");
	}

	private String createLocalVar(String tag) {
		flushOut();
		Integer idx = localVars.get(tag);
		if (idx == null) {
			idx = 0;
		} else {
			idx++;
		}
		localVars.put(tag, idx);
		return format("_%s%s", tag, idx == 0 ? "" : String.valueOf(idx));
	}

	@Override
	public void attributeEnd() {
		printOut("\"");
	}

	@Override
	public void elementEnd(String name) {
		printOut("</", name, ">");
	}

	@Override
	public void elementConditionalStart(String expr, String switchFirstCase) {
		flushOut();
		String switchVar;
		if (Util.isSet(switchFirstCase)) {
			switchVar = createLocalVar("switchVar");
			$("Object ", switchVar, " = ", prefixName(expr, cmpVars.peek().name), ";");
			$("if (java.util.Objects.equals(", switchVar, ", ", prefixName(switchFirstCase, cmpVars.peek().name), ")) {");
			switchHadElseIf.push(true);
		} else {
			switchVar = "";
			ifExprIsTrue(expr);
			switchHadElseIf.push(false);
		}
		switchVars.push(switchVar);
	}

	@Override
	public void elementConditionalElseIf(String expr) {
		flushOut();

		String switchVar = switchVars.peek();
		if (switchVar.isEmpty()) {
			$("} else ");
			ifExprIsTrue(expr);
		} else {
			if (switchHadElseIf.peek()) {
				$("} else ");
			} else {
				switchHadElseIf.pop();
				switchHadElseIf.push(true);
			}

			$("if (java.util.Objects.equals(", switchVar, ", ", prefixName(expr, cmpVars.peek().name), ")) {");
		}
	}

	@Override
	public void elementConditionalElse() {
		flushOut();
		$("} else {");
	}

	@Override
	public void elementConditionalEnd() {
		flushOut();
		$("}");
		switchVars.pop();
		switchHadElseIf.pop();
	}

	@Override
	public void elementRepeatedEnd() {
		flushOut();
		$("}");

		prefixExcludes.pop();
	}

	public void elementRepeatedStart(String[] itemAndListName, Map<ForOfVariable, String> variables) {

		flushOut();

		String itemName = itemAndListName[0];
		String listName = itemAndListName[1];

		String itemType = "Object";
		CmpVar cmpVar = cmpVars.peek();
		boolean found = false;
		for (Field f : cmpVar.cmpClass.getFields()) {
			if (f.getName()
					.equals(listName)) {
				Class<?> type = f.getType();
				itemType = getItemType(f.getGenericType(), type);
				found = true;
				break;
			}
		}

		if (!found) {
			for (Method m : cmpVar.cmpClass.getMethods()) {
				if (format("%s()", m.getName()).equals(listName)) {
					itemType = getItemType(m.getGenericReturnType(), m.getReturnType());
					break;
				}
			}
		}

		Set<String> ex = new HashSet<>(asList(itemName));

		String iterVar = createLocalVar("iter");
		String iterClass = format("%s.%s", IterableWithVariables.class.getName(), "Iter");
		$("for (", iterClass, " ", iterVar, " = ctx.forOfStart(", prefixName(listName, cmpVar.name), ").iterator(); ", iterVar, ".hasNext();) {");
		$(itemType, " ", itemName, " = (", itemType, ")", iterVar, ".next();");

		Set<Entry<ForOfVariable, String>> entries = variables.entrySet();
		for (Map.Entry<ForOfVariable, String> e : entries) {
			String varType = e.getKey() == ForOfVariable.index ? "int" : "boolean";
			$(varType, " ", e.getValue(), "=", iterVar, ".", e.getKey(), ";");
			ex.add(e.getValue());
		}

		prefixExcludes.push(ex);
	}

	private String getItemType(Type genericType, Class<?> type) {
		String itemType = "Object";
		if (type.isArray()) {
			itemType = type.getComponentType()
					.getName();
		} else if (Collection.class.isAssignableFrom(type)) {
			if (genericType instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) genericType;
				itemType = pt.getActualTypeArguments()[0].getTypeName();
			}
		} else {
//						throw new NgoyException("", itemAndListName)
		}
		return itemType;
	}

	private void flushOut() {
		out.flush();
	}

	private void printOutExpr(String s) {
		out.print(s, true, false, null);
	}

	private void printOut(Object... s) {
		String arg = Stream.of(s)
				.map(Object::toString)
				.map(JavaTemplate::escapeJava)
				.collect(joining(""));
		String text = format("%s", arg);
		out.print(text, false, false, null);
	}

	private void ifExprIsTrue(String expr) {
		flushOut();
		$("if (", prefixName(expr, cmpVars.peek().name), ") {");
	}

	public void componentStartInput(CmpRef cmpRef, boolean appRoot, List<CmpInput> params) {
		String cmpClass = cmpRef.clazz.getName()
				.replace('$', '.');

		cmpVar = createLocalVar(cmpRef.clazz.getSimpleName());
		String cmpCall = appRoot ? "cmp" : "cmpNew";
		$(cmpClass, " ", cmpVar, "=(", cmpClass, ")ctx.", cmpCall, "(", cmpClass, ".class);");
		$("{");

		// testForOfNested2
		cmpVars.push(new CmpVar(cmpVar, cmpRef.clazz));
		setInputs(params);
		cmpVars.pop();
	}

	@Override
	public void componentStart(CmpRef cmpRef) {
		cmpVars.push(new CmpVar(cmpVar, cmpRef.clazz));
		$("ctx.cmpInit(", cmpVar, ");");
	}

	@Override
	public void componentEnd() {
		String cmpVar = cmpVars.pop().name;
		flushOut();
		$("ctx.cmpDestroy(", cmpVar, ");");
		$("}");
	}

	@Override
	public void ngContentStart() {
		flushOut();
		cmpVars.push(cmpVars.get(1));
	}

	@Override
	public void ngContentEnd() {
		flushOut();
		cmpVars.pop();
	}

	private String[] parseUnit(String s) {
		int pos = s.lastIndexOf('.');
		if (pos < 0) {
			return new String[] { s, "" };
		}

		String style = s.substring(0, pos);
		String unit = s.substring(pos + 1);
		return new String[] { style, unit };
	}

	private void attributeEval(String attrName, boolean forStyles, List<String[]> exprPairs) {
		String listVar = createLocalVar(format("%slist", forStyles ? "style" : "class"));
		$(Set.class, "<String> ", listVar, " = new ", LinkedHashSet.class, "<String>();");
		for (String[] pair : exprPairs) {
			String clazz = pair[0];
			String expr = pair[1];
			if (expr.isEmpty()) {
				$(listVar, ".add(\"", clazz, "\");");
			} else {
				String ex = prefixName(expr, cmpVars.peek().name);
				if (forStyles) {
					if (clazz.equals("ngStyle")) {
						$("for (", Map.class, ".Entry entry : ", ex, ".entrySet()) {");
						String valueVar = createLocalVar("styleValue");
						$("Object ", valueVar, "=", "entry.getValue();");
						$("  if (", valueVar, "!= null && !", valueVar, ".toString().isEmpty()) {");
						$(listVar, ".add(((String)entry.getKey()).concat(\":\").concat(", valueVar, ".toString()));");
						$("  }");
						$("}");
					} else {
						String[] classAndUnit = parseUnit(clazz);
						String unit = classAndUnit[1];
						clazz = classAndUnit[0];

						String exVar = createLocalVar("expr");
						$("Object ", exVar, "=", ex, ";");
						$("if (", exVar, " != null && !", exVar, ".toString().isEmpty()) {");
						$$(listVar, ".add(\"", clazz, ":\".concat(", exVar, ".toString())");
						if (!unit.isEmpty()) {
							$$(".concat(\"", unit, "\")");
						}
						$(");");
						$("}");
					}
				} else {
					if (clazz.equals("ngClass")) {
						$("for (", Map.class, ".Entry entry : ", ex, ".entrySet()) {");
						$("  if ((Boolean)entry.getValue()) {");
						$(listVar, ".add(entry.getKey());");
						$("  }");
						$("}");
					} else {
						$("if (", ex, ") {");
						$(listVar, ".add(\"", clazz, "\");");
						$("}");
					}
				}
			}
		}

		String delimiter = forStyles ? ";" : " ";

		$("if (!", listVar, ".isEmpty()) {");
		printOut(" ", attrName, "=\"");
		flushOut();
		printOutExpr(format("%s.join(%s, \"%s\")", Ctx.class.getName(), listVar, delimiter));
		printOut("\"");
		flushOut();
		$("}");
	}

	private void setInputs(List<CmpInput> inputs) {
		for (CmpInput input : inputs) {
			InputType inputType = input.type;
			ValueType inputValueType = input.valueType;

			String value = inputValueType == ValueType.EXPR ? prefixName(input.value, cmpVars.get(1).name) : format("\"%s\"", escapeJava(input.value));
			switch (inputType) {
			case FIELD:
				$(cmpVars.peek().name, ".", input.input, "=(", input.inputClass, ")", value, ";");
				break;
			case METHOD:
				$(cmpVars.peek().name, ".", input.input, "(", value, ");");
				break;
			default:
				throw new NgoyException("Unknown input type: %s", inputType);
			}
		}
	}
}
