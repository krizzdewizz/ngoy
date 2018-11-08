package org.ngoy;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.ngoy.internal.util.Util.escapeJava;

import java.io.PrintStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.ngoy.core.internal.Ctx;
import org.ngoy.internal.parser.ParseException;
import org.ngoy.internal.parser.ParserHandler;
import org.ngoy.internal.util.Util;
import org.ngoy.parser.TextOutput;
import org.ngoy.util.CodeBuilder;
import org.ngoy.util.PrintStreamPrinter;

public class JavaTemplate extends CodeBuilder implements ParserHandler {

	private static final Pattern FOR_OF_PATTERN = Pattern.compile("let\\s*(.*)\\s*of\\s*(.*)");

	private final TextOutput out;
	private int nextLocalVarIndex;

	public JavaTemplate(PrintStream prn) {
		super(new PrintStreamPrinter(prn));
		out = new TextOutput(printer);
	}

	@Override
	public void documentStart() {
		$("package org.ngoy;");
		$("public class X {");
		$("  public static void render(", Ctx.class, " ctx) throws Exception {");
	}

	@Override
	public void documentEnd() {
		flushOut();
		$("  }");
		$("}");
	}

	@Override
	public void text(String text, boolean textIsExpr, List<String[]> pipes) {
		if (text.isEmpty()) {
			return;
		}
		if (textIsExpr) {
			String params;
			if (!pipes.isEmpty()) {
				String pp = pipes.stream()
						.map(p -> {
							String ppa = p.length > 1 ? format(",\"%s\"", escapeJava(p[1])) : "";
							String s = format("new String[]{\"%s\"%s}", p[0], ppa);
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
	public void elementHeadEnd() {
		printOut(">");
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
		String localVar = format("_$$l%s", nextLocalVarIndex++);
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
	public void elementConditionalStart(String expr) {
		flushOut();
		ifExprIsTrue(expr);
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
	}

	@Override
	public void elementRepeatedEnd() {
		flushOut();
		$("ctx.popContext();");
		$("}");
	}

	public void elementRepeatedStart(String expr) {
		Matcher matcher = FOR_OF_PATTERN.matcher(expr);
		if (!matcher.find()) {
			throw new ParseException("*ngFor expression malformed: %s", expr);
		}

		flushOut();

		String itemName = matcher.group(1)
				.trim();
		String listName = matcher.group(2)
				.trim();

		String itemVar = createLocalVar();
		$("for (Object ", itemVar, ": ctx.evalIterable(\"", listName, "\")) {");
		$("  ctx.pushContext(\"", itemName, "\", ", itemVar, ");");
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
	public void componentStart(String clazz, List<String> params) {
		$("ctx.pushCmpContext(\"", clazz, "\",", flattenStrings(params), ");");
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
