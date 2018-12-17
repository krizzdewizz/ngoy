package ngoy.internal.parser.template;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static ngoy.core.Util.getLine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import ngoy.core.Injector;
import ngoy.core.NgoyException;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;
import ngoy.core.Util;
import ngoy.core.Variable;
import ngoy.core.internal.CmpRef;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.IteratorWithVariables;
import ngoy.core.internal.TemplateRender;
import ngoy.internal.parser.ExprParser;
import ngoy.internal.parser.FieldAccessToGetterParser.ClassDef;
import ngoy.internal.parser.FieldAccessToGetterParser.ListItemDef;
import ngoy.internal.parser.ForOfVariable;
import ngoy.internal.parser.Inputs.CmpInput;
import ngoy.internal.parser.Inputs.InputType;
import ngoy.internal.parser.Inputs.ValueType;
import ngoy.internal.parser.ParserHandler;

public class JavaTemplate extends CodeBuilder implements ParserHandler {

	private static final String EXPR_COMMENT_TAG = "//EXPR:::";

	public static class ExprComment {
		public final String comment;
		public final String sourcePosition;

		public ExprComment(String comment, String sourcePosition) {
			this.comment = comment;
			this.sourcePosition = sourcePosition;
		}
	}

	public static ExprComment getExprComment(String code, int lineNumber) {
		String line = getLine(code, lineNumber - 1).trim();
		if (!line.startsWith(EXPR_COMMENT_TAG)) {
			return new ExprComment(line, "");
		}
		String s = line.substring(EXPR_COMMENT_TAG.length());
		String delim = ":::";
		int pos = s.indexOf(delim);
		String source = "";
		if (pos >= 0) {
			source = s.substring(0, pos);
			s = s.substring(pos + delim.length());
		}
		return new ExprComment(s, source);
	}

	private static final String STRINGS = "$STRINGS$";

	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\r|\\n");

	private static class CmpVar {
		private String name;
		private Class<?> cmpClass;

		public CmpVar(String name, Class<?> cmpClass) {
			this.name = name;
			this.cmpClass = cmpClass;
		}
	}

	static final String CTX_VAR = "__";

	private final TextOutput out;
	private String textOverrideVar;
	private boolean hadTextOverride;
	private final LinkedList<String> switchVars = new LinkedList<>();
	private final LinkedList<Boolean> switchHadElseIf = new LinkedList<>();
	private final LinkedList<CmpVar> cmpVars = new LinkedList<>();
	private final LinkedList<Map<String, Class<?>>> localVarDefs = new LinkedList<>();
	private final LinkedList<Set<String>> prefixExcludes = new LinkedList<>();
	private final Set<String> pipeNames = new HashSet<>();
	private final Map<String, Integer> localVars = new HashMap<>();
	private final Map<String, Integer> stringRefs = new LinkedHashMap<>();
	private final Map<String, Variable<?>> variables;
	private final boolean bodyOnly;
	private String cmpVar;
	private String code;

	private String stringsVar;
	private String stringsLocalVar;

	private String sourcePosition;

	public JavaTemplate(String contentType, boolean bodyOnly, Map<String, Variable<?>> variables) {
		super(new Printer());
		this.bodyOnly = bodyOnly;
		this.variables = variables;
		out = new TextOutput(printer, () -> depth, this::createStringRef, contentType);
		stringsVar = createLocalVar("strings", false);
		stringsLocalVar = createLocalVar("stringsLocal", false);
	}

	private String createStringRef(String text) {
		Integer ref = stringRefs.get(text);
		if (ref == null) {
			ref = stringRefs.size();
			stringRefs.put(text, ref);
		}

		return format("%s[%s]", stringsLocalVar, ref);
	}

	@Override
	public void documentStart(List<Class<?>> pipes) {
		if (!bodyOnly) {
			$("package ngoy;");
			$("@SuppressWarnings(\"all\")");
			$("public class X {");
		}

		$("public static ", TemplateRender.class, " createRenderer(", Injector.class, " injector) { return new Renderer(injector); }");

		addApiHelpers();

		$("private static class Renderer implements ", TemplateRender.class, "{");

		addPipeMethods(pipes);

		$("private Renderer(", Injector.class, " injector) {");
		setPipes(pipes);
		$("}");

		$("private static final String[] ", stringsVar, "= new String[]{", STRINGS, "};");

		$("public void render(", Ctx.class, " ", CTX_VAR, ") throws Exception {");
		$("String[] ", stringsLocalVar, "=", stringsVar, ";");
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
			$(var.type, " ", name, "=(", var.type, ")", CTX_VAR, ".getVariableValue(\"", name, "\");");
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
			$("_", pipeFun, "=(", PipeTransform.class, ")injector.get(", pipe, ".class);");
		}
	}

	@Override
	public void documentEnd() {
		flushOut();
		$("}"); // render method

		$("}"); // class Renderer

		if (!bodyOnly) {
			$("}");
		}

		code = super.toString();

		replaceStrings();
	}

	@Override
	public String toString() {
		return code;
	}

	private void replaceStrings() {
		code = code.replace(STRINGS, stringRefs.keySet()
				.stream()
				.map(s -> format("\"%s\"", s))
				.collect(joining(",\n")));
	}

	@Override
	public void text(String text, boolean textIsExpr, boolean escape) {
		if (text.isEmpty()) {
			return;
		}
		if (textIsExpr) {
			printExprComment(text);
			printOutExpr(prefixName(text, cmpVars.peek().name));
		} else {
			if (escape) {
				out.print(text, false, true);
			} else {
				printOut(text);
			}
		}
	}

	private String prefixName(String expr, String prefix) {
		return prefixName(expr, prefix, null);
	}

	private String prefixName(String expr, String prefix, ClassDef[] outLastClassDef) {
		Set<String> excludes = new HashSet<>(pipeNames);
		excludes.addAll(variables.keySet());
		excludes.add("java");
		excludes.add("Map");
		excludes.add("List");
		Set<String> more = prefixExcludes.peek();
		if (more != null) {
			excludes.addAll(more);
		}

		Map<String, Class<?>> vars = new HashMap<>();
		for (Map<String, Class<?>> it : localVarDefs) {
			vars.putAll(it);
		}

		return ExprParser.prefixName(cmpVars.peek().cmpClass, vars, expr, prefix, excludes, variables, outLastClassDef);
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
		printExprComment(expr);
		$(textOverrideVar, "=(String)", prefixName(expr, cmpVars.peek().name), ";");
		hadTextOverride = true;
	}

	@Override
	public void elementHeadEnd() {
		printOut(">");

		if (hadTextOverride) {
			flushOut();

			out.printEscaped(textOverrideVar);
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
		printExprComment(expr);
		$("Object ", evalResultVar, "=", prefixName(expr, cmpVars.peek().name), ";");

		$("if(", evalResultVar, "!=null) {");
		printOut(" ", name, "=\"");
		flushOut();
		out.printEscaped(evalResultVar);
		printOut("\"");
		flushOut();
		$("}");
	}

	private String createLocalVar(String tag) {
		return createLocalVar(tag, true);
	}

	private String createLocalVar(String tag, boolean flush) {
		if (flush) {
			flushOut();
		}
		Integer idx = localVars.get(tag);
		if (idx == null) {
			idx = 0;
		} else {
			idx++;
		}
		localVars.put(tag, idx);
		return format("__%s%s", tag, idx == 0 ? "" : String.valueOf(idx));
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
			printExprComment(expr);
			$("Object ", switchVar, "=", prefixName(expr, cmpVars.peek().name), ";");
			printExprComment(switchFirstCase);
			$("if(java.util.Objects.equals(", switchVar, ", ", prefixName(switchFirstCase, cmpVars.peek().name), ")) {");
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

			printExprComment(expr);
			$("if(java.util.Objects.equals(", switchVar, ", ", prefixName(expr, cmpVars.peek().name), ")) {");
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
		localVarDefs.pop();
	}

	public void elementRepeatedStart(String[] itemAndListName, Map<ForOfVariable, String> variables) {

		flushOut();

		String itemName = itemAndListName[0];
		String listName = itemAndListName[1];

		ClassDef[] outLastClassDef = new ClassDef[1];
		listName = prefixName(listName, "", outLastClassDef);

		ClassDef listClass = outLastClassDef[0];
		if (!listClass.valid()) {
			throw new NgoyException("'%s' is not iterable. Must be an instance of %s or an array", listName, Iterable.class.getName());
		}

		ListItemDef itemDef = listClass.getListItemType(listClass);

		Set<String> ex = new HashSet<>(asList(itemName));

		CmpVar cmpVar = cmpVars.peek();

		String itemClazz = Util.primitiveToRefType(itemDef.clazz);

		String iterVar = createLocalVar("iter");
		printExprComment(listName);
		$("for (", IteratorWithVariables.class, " ", iterVar, "= new ", IteratorWithVariables.class, "(", prefixName(listName, cmpVar.name), "); ", iterVar, ".hasNext();) {");
		$(itemDef.typeName, " ", itemName, "=(", itemClazz, ")", iterVar, ".next();");

		Set<Entry<ForOfVariable, String>> entries = variables.entrySet();
		for (Map.Entry<ForOfVariable, String> e : entries) {
			String varType = e.getKey() == ForOfVariable.index ? "int" : "boolean";
			$(varType, " ", e.getValue(), "=", iterVar, ".", e.getKey(), ";");
			ex.add(e.getValue());
		}

		prefixExcludes.push(ex);

		Map<String, Class<?>> iterVarDef = new HashMap<>();
		iterVarDef.put(itemName, itemDef.clazz);
		localVarDefs.push(iterVarDef);
	}

	private void flushOut() {
		out.flush();
	}

	private void printOutExpr(String s) {
		out.print(s, true, false);
	}

	private void printExprComment(String expr) {
		out.flush();
		$(EXPR_COMMENT_TAG, sourcePosition, ":::", NEWLINE_PATTERN.matcher(expr)
				.replaceAll(""));
	}

	private void printOut(Object... s) {
		String arg = Stream.of(s)
				.map(Object::toString)
				.map(Util::escapeJava)
				.collect(joining(""));
		String text = format("%s", arg);
		out.print(text, false, false);
	}

	private void ifExprIsTrue(String expr) {
		flushOut();
		printExprComment(expr);
		$("if(", prefixName(expr, cmpVars.peek().name), ") {");
	}

	public void componentStartInput(CmpRef cmpRef, boolean appRoot, List<CmpInput> params) {
		String cmpClass = cmpRef.clazz.getName()
				.replace('$', '.');

		cmpVar = createLocalVar(cmpRef.clazz.getSimpleName());
		String cmpCall = appRoot ? "cmp" : "cmpNew";
		$(cmpClass, " ", cmpVar, "=(", cmpClass, ")", CTX_VAR, ".", cmpCall, "(", cmpClass, ".class);");
		$("{");

		// testForOfNested2
		cmpVars.push(new CmpVar(cmpVar, cmpRef.clazz));
		setInputs(params);
		cmpVars.pop();
	}

	@Override
	public void componentStart(CmpRef cmpRef) {
		cmpVars.push(new CmpVar(cmpVar, cmpRef.clazz));
		$("", CTX_VAR, ".cmpInit(", cmpVar, ");");
	}

	@Override
	public void componentEnd() {
		String cmpVar = cmpVars.pop().name;
		flushOut();
		$("", CTX_VAR, ".cmpDestroy(", cmpVar, ");");
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
		String delimiter = forStyles ? ";" : " ";
		String listVar = createLocalVar(format("%slist", forStyles ? "style" : "class"));
		$(StringBuilder.class, " ", listVar, "=new ", StringBuilder.class, "();");
		for (String[] pair : exprPairs) {
			String clazz = pair[0];
			String expr = pair[1];
			if (expr.isEmpty()) {
				$("if(", listVar, ".length()>0){", listVar, ".append(\"", delimiter, "\");}");
				$(listVar, ".append(\"", clazz, "\");");
			} else {
				String ex = prefixName(expr, cmpVars.peek().name);
				if (forStyles) {
					if (clazz.equals("ngStyle")) {
						$("for (", Map.class, ".Entry entry : ", ex, ".entrySet()) {");
						String valueVar = createLocalVar("styleValue");
						$("Object ", valueVar, "=", "entry.getValue();");
						$("  if(", valueVar, "!= null && !", valueVar, ".toString().isEmpty()) {");
						$("if(", listVar, ".length()>0){", listVar, ".append(\"", delimiter, "\");}");
						$(listVar, ".append(((String)entry.getKey()).concat(\":\").concat(", valueVar, ".toString()));");
						$("  }");
						$("}");
					} else {
						String[] classAndUnit = parseUnit(clazz);
						String unit = classAndUnit[1];
						clazz = classAndUnit[0];

						String exVar = createLocalVar("expr");
						printExprComment(ex);
						$("Object ", exVar, "=", ex, ";");
						$("if(", exVar, "!=null && !", exVar, ".toString().isEmpty()) {");
						$("if(", listVar, ".length()>0){", listVar, ".append(\"", delimiter, "\");}");
						$$(listVar, ".append(\"", clazz, ":\".concat(", exVar, ".toString())");
						if (!unit.isEmpty()) {
							$$(".concat(\"", unit, "\")");
						}
						$(");");
						$("}");
					}
				} else {
					if (clazz.equals("ngClass")) {
						$("for (", Map.class, ".Entry entry : ", ex, ".entrySet()) {");
						$("  if((Boolean)entry.getValue()) {");
						$("if(", listVar, ".length()>0){", listVar, ".append(\"", delimiter, "\");}");
						$(listVar, ".append(entry.getKey());");
						$("  }");
						$("}");
					} else {
						printExprComment(ex);
						$("if(", ex, ") {");
						$("if(", listVar, ".length()>0){", listVar, ".append(\"", delimiter, "\");}");
						$(listVar, ".append(\"", clazz, "\");");
						$("}");
					}
				}
			}
		}

		$("if(", listVar, ".length()>0) {");
		printOut(" ", attrName, "=\"");
		flushOut();
		printOutExpr(listVar);
		printOut("\"");
		flushOut();
		$("}");
	}

	private void setInputs(List<CmpInput> inputs) {
		for (CmpInput input : inputs) {
			InputType inputType = input.type;
			ValueType inputValueType = input.valueType;

			printExprComment(input.value);
			String value = inputValueType == ValueType.EXPR ? prefixName(input.value, cmpVars.get(1).name) : format("\"%s\"", Util.escapeJava(input.value));
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

	@Override
	public void setSourcePosition(String sourcePosition) {
		this.sourcePosition = sourcePosition;
	}
}
