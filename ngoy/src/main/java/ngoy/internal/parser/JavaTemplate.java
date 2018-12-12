package ngoy.internal.parser;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;

import ngoy.core.NgoyException;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;
import ngoy.core.Util;
import ngoy.core.internal.CmpRef;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.IterableWithVariables;
import ngoy.internal.parser.Inputs.CmpInput;
import ngoy.internal.parser.Inputs.InputType;
import ngoy.internal.parser.Inputs.ValueType;
import ngoy.parser.TextOutput;
import ngoy.util.CodeBuilder;
import ngoy.util.PrintStreamPrinter;

public class JavaTemplate extends CodeBuilder implements ParserHandler {

	public static String escapeJava(String text) {
		return StringEscapeUtils.escapeJava(text);
	}

	static class CmpVar {
		private String name;
		private Class<?> cmpClass;

		public CmpVar(String name, Class<?> cmpClass) {
			this.name = name;
			this.cmpClass = cmpClass;
		}
	}

	private final TextOutput out;
	private int nextLocalVarIndex;
	private String textOverrideVar;
	private boolean hadTextOverride;
	private final LinkedList<String> switchVars = new LinkedList<>();
	private final LinkedList<Boolean> switchHadElseIf = new LinkedList<>();
	private final LinkedList<CmpVar> cmpVars = new LinkedList<>();
	private final Set<String> pipeNames = new HashSet<>();
	private final LinkedList<Set<String>> prefixExcludes = new LinkedList<>();
	private final boolean bodyOnly;

	public JavaTemplate(PrintStream prn, boolean bodyOnly) {
		super(new PrintStreamPrinter(prn));
		this.bodyOnly = bodyOnly;
		out = new TextOutput(printer);
	}

	@Override
	public void documentStart(List<Class<?>> pipes) {
		if (!bodyOnly) {
			$("package ngoy;");
			$("@SuppressWarnings(\"all\")");
			$("public class X {");
		}

		addPipeMethods(pipes);
		$("  public static void render(", Ctx.class, " ctx) throws Exception {");
		setPipes(pipes);

		textOverrideVar = createLocalVar("textOverride");
		$("String ", textOverrideVar, ";");
	}

	private void addPipeMethods(List<Class<?>> pipes) {
		for (Class<?> pipe : pipes) {
			String pipeName = pipe.getAnnotation(Pipe.class)
					.value();
			String pipeFun = format("$%s", pipeName);
			pipeNames.add(pipeFun);
			$$("private static ", PipeTransform.class, " __", pipeFun, ";");
			$("private static Object ", pipeFun, "(Object obj, Object... args) {");
			$("  return __", pipeFun, ".transform(obj, args);");
			$("}");
		}
	}

	private void setPipes(List<Class<?>> pipes) {
		for (Class<?> pipe : pipes) {
			String pipeName = pipe.getAnnotation(Pipe.class)
					.value();
			String pipeFun = format("$%s", pipeName);
			$("__", pipeFun, " = ctx.getPipe(\"", pipeName, "\");");
		}
	}

	@Override
	public void documentEnd() {
		flushOut();
		$("  }"); // render

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
			String prefixed = prefixName(text, cmpVars.peek().name);
			printOutExpr(prefixed);
		} else {
			if (escape) {
				out.printEscaped(escapeJava(text), false);
			} else {
				printOut(text);
			}
		}
	}

	private String prefixName(String expr, String prefix) {
		Set<String> ex = new HashSet<>(pipeNames);
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
		$(textOverrideVar, "=", "(String)ctx.eval(\"", expr, "\");");
		hadTextOverride = true;
	}

	@Override
	public void elementHeadEnd() {
		printOut(">");

		if (hadTextOverride) {
			flushOut();

			out.printEscaped(textOverrideVar, true);
			$(textOverrideVar, "= null;");

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
		String evalResultVar = createLocalVar("evalResult");
		$("Object ", evalResultVar, ";");

		// qq
//		$(evalResultVar, " = ctx.eval(\"", escapeJava(expr), "\");");
		$(evalResultVar, " = (", escapeJava(expr), ");");

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
		String localVar = format("_$l_%s_%s", tag, nextLocalVarIndex++);
		return localVar;
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
			$("Object ", switchVar, " = ctx.eval(\"", expr, "\");");
		} else {
			switchVar = "";
			ifExprIsTrue(expr);
		}
		switchVars.push(switchVar);
		switchHadElseIf.push(false);
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

			$("if (java.util.Objects.equals(", switchVar, ", ctx.eval(\"", expr, "\"))) {");
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

	public void componentStartInput(CmpRef cmpRef, List<CmpInput> params) {
		String cmpClass = cmpRef.clazz.getName()
				.replace('$', '.');
		String cmpVar = createLocalVar("cmp");
		cmpVars.push(new CmpVar(cmpVar, cmpRef.clazz));
		$(cmpClass, " ", cmpVar, "=(", cmpClass, ")ctx.pushCmpContextInput(", cmpClass, ".class);");
		$("{");
		setInputs(params);
	}

	@Override
	public void componentStart(CmpRef cmpRef) {
		$("ctx.pushCmpContext(", cmpVars.peek().name, ");");
	}

	@Override
	public void componentEnd() {
		String cmpVar = cmpVars.pop().name;
		flushOut();
		$("ctx.popCmpContext(", cmpVar, ");");
		$("}");
	}

	@Override
	public void ngContentStart() {
		flushOut();
		$("ctx.pushParentContext();");
	}

	@Override
	public void ngContentEnd() {
		flushOut();
		$("ctx.popContext();");
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
		$(List.class, "<String> ", listVar, " = new ", ArrayList.class, "<>();");
		for (String[] pair : exprPairs) {
			String clazz = pair[0];
			String expr = pair[1];
			if (expr.isEmpty()) {
				$(listVar, ".add(\"", clazz, "\");");
			} else {
				String ex = prefixName(expr, cmpVars.peek().name);
				if (forStyles) {

					String[] classAndUnit = parseUnit(clazz);
					String unit = classAndUnit[1];
					clazz = classAndUnit[0];

					String exVar = createLocalVar("expr");
					$("Object ", exVar, "=", ex, ";");
					$("if (", exVar, " != null) {");
					$$(listVar, ".add(\"", clazz, ":\".concat(", exVar, ".toString())");
					if (!unit.isEmpty()) {
						$$(".concat(\"", unit, "\")");
					}
					$(");");
				} else {
					$("if (", ex, ") {");
					$(listVar, ".add(\"", clazz, "\");");
				}
				$("}");
			}
		}

		String delimiter = forStyles ? ";" : " ";

		$("if (!", listVar, ".isEmpty()) {");
		printOut(" ", attrName, "=\"");
		flushOut();
		printOutExpr(format("ctx.join(%s, \"%s\")", listVar, delimiter));
		printOut("\"");
		flushOut();
		$("}");
	}

	private void setInputs(List<CmpInput> inputs) {
		for (CmpInput input : inputs) {
			InputType inputType = input.type;
			ValueType inputValueType = input.valueType;

			String value = inputValueType == ValueType.EXPR ? prefixName(input.value, cmpVars.get(1).name) : escapeJava(input.value);
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
