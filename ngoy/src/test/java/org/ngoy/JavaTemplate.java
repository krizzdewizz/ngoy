package org.ngoy;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.ngoy.core.Util.escapeJava;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.ngoy.core.Util;
import org.ngoy.core.internal.CmpRef;
import org.ngoy.core.internal.Ctx;
import org.ngoy.internal.parser.ForOfVariable;
import org.ngoy.internal.parser.ParserHandler;
import org.ngoy.parser.TextOutput;
import org.ngoy.util.CodeBuilder;
import org.ngoy.util.PrintStreamPrinter;

public class JavaTemplate extends CodeBuilder implements ParserHandler {

	private final TextOutput out;
	private int nextLocalVarIndex;
	private String textOverrideVar;
	private boolean hadTextOverride;
	private LinkedList<String> switchVars = new LinkedList<>();
	private LinkedList<Boolean> switchHadElseIf = new LinkedList<>();

	public JavaTemplate(PrintStream prn) {
		super(new PrintStreamPrinter(prn));
		out = new TextOutput(printer);
	}

	@Override
	public void documentStart() {
		$("package org.ngoy;");
		$("public class X {");
		$("  public static void render(", Ctx.class, " ctx) throws Exception {");

		textOverrideVar = createLocalVar();
		$("String ", textOverrideVar, ";");
	}

	@Override
	public void documentEnd() {
		flushOut();
		$("  }");
		$("}");
	}

	@Override
	public void text(String text, boolean textIsExpr, List<List<String>> pipes) {
		if (text.isEmpty()) {
			return;
		}
		if (textIsExpr) {
			String params;
			if (!pipes.isEmpty()) {
				String pp = pipes.stream()
						.map(p -> {
							String ppa = p.size() > 1 ? format(",\"%s\"", escapeJava(p.get(1))) : "";
							String s = format("new String[]{\"%s\"%s}", p.get(0), ppa);
							return s;
						})
						.collect(joining(","));
				params = format(", %s", pp);
			} else {
				params = "";
			}
			String call = format("ctx.eval(\"%s\"%s)", escapeJava(text), params);
			printOutExpr(call);
		} else {
			out.printEscaped(escapeJava(text), false);
		}
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
		String classListVar = createLocalVar();

		String pp = classExprPairs.stream()
				.map(p -> {
					String s = format("new String[]{\"%s\", \"%s\"}", escapeJava(p[0]), escapeJava(p[1]));
					return s;
				})
				.collect(joining(","));

		$("Object ", classListVar, "= ctx.evalClasses(", pp, ");");

		$("if (", classListVar, " != null) {");
		printOut(" class=\"");
		flushOut();
		printOutExpr(classListVar);
		printOut("\"");
		flushOut();
		$("}");
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
		String evalResultVar = createLocalVar();
		$("Object ", evalResultVar, ";");

		$(evalResultVar, " = ctx.eval(\"", escapeJava(expr), "\");");

		$("if (", evalResultVar, " != null) {");
		printOut(" ", name, "=\"");
		flushOut();
		out.printEscaped(evalResultVar, true);
		printOut("\"");
		flushOut();
		$("}");
	}

	private String createLocalVar() {
		flushOut();
		String localVar = format("_$l%s", nextLocalVarIndex++);
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
			switchVar = createLocalVar();
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
		$("ctx.popContext();");
		$("}");

		$("ctx.forOfEnd();");
	}

	public void elementRepeatedStart(String[] itemAndListName, Map<ForOfVariable, String> variables) {

		flushOut();

		String itemName = itemAndListName[0];
		String listName = itemAndListName[1];

		String arr = createLocalVar();
		$("String []", arr, " = new String[", 2 * variables.size(), "];");
		int i = 0;
		Set<Entry<ForOfVariable, String>> entries = variables.entrySet();
		for (Map.Entry<ForOfVariable, String> e : entries) {
			$(arr, "[", i, "] = \"", e.getKey(), "\";");
			$(arr, "[", i + 1, "] = \"", e.getValue(), "\";");
			i += 2;
		}

		String itemVar = createLocalVar();
		$("for (Object ", itemVar, ": ctx.forOfStart(\"", listName, "\", ", arr, ")) {");
		$("  ctx.pushForOfContext(\"", itemName, "\", ", itemVar, ");");
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
				.map(Util::escapeJava)
				.collect(joining(""));
		String text = format("%s", arg);
		out.print(text, false, false, null);
	}

	private void ifExprIsTrue(String expr) {
		flushOut();
		$("if (ctx.evalBool(\"", escapeJava(expr), "\")) {");
	}

	@Override
	public void componentStart(CmpRef cmpRef, List<String> params) {
		String ps = flattenStrings(params);
		$$("ctx.pushCmpContext(\"", cmpRef.clazz.getName(), "\"");
		if (!ps.isEmpty()) {
			$$(",");
			$$(ps);
		}
		$(");");
	}

	private String flattenStrings(List<String> params) {
		return params.stream()
				.map(p -> format("\"%s\"", escapeJava(p)))
				.collect(joining(","));
	}

	@Override
	public void componentEnd() {
		flushOut();
		$("ctx.popCmpContext();");
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

}
