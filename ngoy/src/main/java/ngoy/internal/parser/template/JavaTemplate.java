package ngoy.internal.parser.template;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static ngoy.core.Util.escapeJava;
import static ngoy.core.Util.getLine;
import static ngoy.core.Util.isSet;
import static ngoy.core.Util.primitiveToRefType;
import static ngoy.core.Util.sourceClassName;
import static ngoy.core.Util.tryLoadClass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import ngoy.core.Injector;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;
import ngoy.core.OnRender;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;
import ngoy.core.Util;
import ngoy.core.Variable;
import ngoy.core.internal.CmpRef;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.IteratorWithVariables;
import ngoy.core.internal.RenderException;
import ngoy.core.internal.TemplateRender;
import ngoy.internal.parser.ClassDef;
import ngoy.internal.parser.ExprParser;
import ngoy.internal.parser.ForOfDef;
import ngoy.internal.parser.ForOfVariable;
import ngoy.internal.parser.Inputs.CmpInput;
import ngoy.internal.parser.Inputs.InputType;
import ngoy.internal.parser.Inputs.ValueType;
import ngoy.internal.parser.ParserHandler;

public class JavaTemplate extends CodeBuilder implements ParserHandler {

	private static final String DELIM = ":::";
	private static final String EXPR_COMMENT_TAG = "//EXPR".concat(DELIM);

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
		return getExprComment(s);
	}

	public static ExprComment getExprComment(String debugInfo) {
		int pos = debugInfo.indexOf(DELIM);
		String source = "";
		if (pos >= 0) {
			source = debugInfo.substring(0, pos);
			debugInfo = debugInfo.substring(pos + DELIM.length());
		}
		return new ExprComment(debugInfo, source);
	}

	private static final String STRINGS = "$STRINGS$";
	private static final String PIPE_METHODS = "$PIPE_METHODS$";
	private static final String SET_PIPES = "$SET_PIPES$";

	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\r|\\n");

	private static class CmpVar {
		private final String name;
		private final Class<?> cmpClass;

		public CmpVar(String name, Class<?> cmpClass) {
			this.name = name;
			this.cmpClass = cmpClass;
		}
	}

	private static final String CMP_INSTANCE_VAR = "__cmpInstance__";
	public static final String CTX_VAR = "__";
	private static final Set<String> GLOBALS = new HashSet<>(asList("java", "Map", "List", "Set", CTX_VAR));

	private final TextOutput out;
	private String textOverrideVar;
	private boolean hadTextOverride;
	private final LinkedList<String> switchVars = new LinkedList<>();
	private final LinkedList<Boolean> switchHadElseIf = new LinkedList<>();
	private final LinkedList<CmpVar> cmpVars = new LinkedList<>();
	private final LinkedList<Map<String, Class<?>>> localVarDefs = new LinkedList<>();
	private final LinkedList<Set<String>> prefixExcludes = new LinkedList<>();
	private final Map<String, Integer> localVars = new HashMap<>();
	private final Map<String, Integer> stringRefs = new LinkedHashMap<>();
	private final Map<String, Variable<?>> variables;
	private final Set<String> methodCalls = new HashSet<>();
	private String cmpVar;
	private String code;

	private String stringsVar;
	private String stringsLocalVar;
	private String lastExprVar;

	private String sourcePosition;
	private Map<String, Class<?>> pipesMap;

	public JavaTemplate(String contentType, Map<String, Variable<?>> variables) {
		this.variables = variables;
		out = new TextOutput(printer, this::getDepth, this::createStringRef, contentType);
		stringsVar = createLocalVar("strings", false);
		stringsLocalVar = createLocalVar("stringsLocal", false);
		lastExprVar = createLocalVar("lastExpr", false);
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

		pipesMap = pipes.stream()
				.collect(toMap(pipe -> format("$%s", pipe.getAnnotation(Pipe.class)
						.value()), Objects::requireNonNull));

		$("public static ", TemplateRender.class, " createRenderer(", Injector.class, " injector) { return new Renderer(injector); }");

		addApiHelpers();

		$("private static class Renderer implements ", TemplateRender.class, "{");

		$(PIPE_METHODS);

		$("private Renderer(", Injector.class, " injector){");
		$(SET_PIPES);
		$("}");

		$("private static final String[] ", stringsVar, "=new String[]{", STRINGS, "};");

		$("public void render(", Ctx.class, " ", CTX_VAR, ", Object ", CMP_INSTANCE_VAR, ") throws ", RenderException.class, "{");
		$("String ", lastExprVar, "=\"\";");
		$("try{");
		$("final String[] ", stringsLocalVar, "=", stringsVar, ";");
		addVariables();

		textOverrideVar = createLocalVar("textOverride");
		$("String ", textOverrideVar, ";");
	}

	private void addApiHelpers() {
		$("private static <K,V> ", Map.class, "<K,V> Map(Object...pairs){");
		$(" return ", Ctx.class, ".<K,V>Map(pairs);");
		$("}");

		$("private static <T> ", List.class, " List(T...items){");
		$(" return ", Ctx.class, ".<T>List(items);");
		$("}");

		$("private static <T> ", Set.class, " Set(T...items){");
		$(" return ", Ctx.class, ".<T>Set(items);");
		$("}");
	}

	private void addVariables() {
		for (Entry<String, Variable<?>> vars : variables.entrySet()) {
			Variable<?> var = vars.getValue();
			String name = vars.getKey();
			$(var.type, " ", name, "=(", var.type, ")", CTX_VAR, ".getVariableValue(\"", name, "\");");
		}
	}

	@Override
	public void documentEnd() {
		flushOut();
		$("} catch (Exception __e){"); // try
		$(" throw new ", RenderException.class, "(__e, ", lastExprVar, ");");
		$("}"); // catch
		$("}"); // render method

		$("}"); // class Renderer

		code = super.toString();

		replaceStrings();
		replacePipes();
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

	private void replacePipes() {

		Set<String> pipeCalls = getPipeCalls();

		String pipeMethods = new CodeBuilder() {
			@Override
			protected void doCreate() {
				for (String pipeFun : pipeCalls) {
					$("private static ", PipeTransform.class, " _", pipeFun, ";");
					$("private static Object ", pipeFun, "(Object obj, Object... args){");
					$("  return _", pipeFun, ".transform(obj, args);");
					$("}");
				}
			}
		}.create()
				.toString();

		String setPipes = new CodeBuilder() {
			@Override
			protected void doCreate() {
				for (String pipeFun : pipeCalls) {
					$("_", pipeFun, "=(", PipeTransform.class, ")injector.get(", pipesMap.get(pipeFun), ".class);");
				}
			}
		}.create()
				.toString();

		code = code.replace(PIPE_METHODS, pipeMethods)
				.replace(SET_PIPES, setPipes);
	}

	private Set<String> getPipeCalls() {
		return methodCalls.stream()
				.filter(it -> pipesMap.containsKey(it))
				.collect(toSet());
	}

	@Override
	public void text(String text, boolean textIsExpr, boolean escape) {
		if (text.isEmpty()) {
			return;
		}
		if (textIsExpr) {
			printExprComment(text);
			printOutExpr(prefixName(text));
		} else {
			if (escape) {
				out.print(text, false, true);
			} else {
				printOut(text);
			}
		}
	}

	private String prefixName(String expr) {
		return prefixName(expr, null, null);
	}

	private String prefixName(String expr, @Nullable CmpVar cmpVar, @Nullable ClassDef[] outLastClassDef) {
		Set<String> excludes = new HashSet<>(pipesMap.keySet());
		excludes.addAll(variables.keySet());
		excludes.addAll(GLOBALS);

		Set<String> more = prefixExcludes.peek();
		if (more != null) {
			excludes.addAll(more);
		}

		Map<String, Class<?>> vars = new HashMap<>();
		for (Map<String, Class<?>> it : localVarDefs) {
			vars.putAll(it);
		}

		if (cmpVar == null) {
			cmpVar = cmpVars.peek();
		}

		return ExprParser.prefixName(cmpVar.cmpClass, vars, expr, cmpVar.name, excludes, variables, outLastClassDef, methodCalls);
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
		String retVar = createLocalVar("textOverrideRet");
		$("final Object ", retVar, "=", prefixName(expr), ";");
		$(textOverrideVar, "=", retVar, "==null?null:", retVar, ".toString();");
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
		$("final Object ", evalResultVar, "=", prefixName(expr), ";");

		$("if(", evalResultVar, "!=null){");
		printOut(" ", name, "=\"");
		flushOut();
		out.printEscaped(evalResultVar);
		printOut("\"");
		flushOut();
		$("}");
	}

	private String createLocalVar(String name) {
		return createLocalVar(name, true);
	}

	private String createLocalVar(String name, boolean flush) {
		if (flush) {
			flushOut();
		}
		Integer idx = localVars.get(name);
		if (idx == null) {
			idx = 0;
		} else {
			idx++;
		}
		localVars.put(name, idx);
		return format("__%s%s", name, idx == 0 ? "" : String.valueOf(idx));
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
		if (isSet(switchFirstCase)) {
			switchVar = createLocalVar("switchVar");
			printExprComment(expr);
			$("final Object ", switchVar, "=", prefixName(expr), ";");
			printExprComment(switchFirstCase);
			$("if(java.util.Objects.equals(", switchVar, ", ", prefixName(switchFirstCase), ")){");
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
			$("}else ");
			ifExprIsTrue(expr);
		} else {
			if (switchHadElseIf.peek()) {
				$("}else ");
			} else {
				switchHadElseIf.pop();
				switchHadElseIf.push(true);
			}

			printExprComment(expr);
			$("if(java.util.Objects.equals(", switchVar, ", ", prefixName(expr), ")){");
		}
	}

	@Override
	public void elementConditionalElse() {
		flushOut();
		$("}else{");
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

	@Override
	public void elementRepeatedStart(ForOfDef forOfDef, Map<ForOfVariable, String> variables) {
		flushOut();

		String itemName = forOfDef.itemName;
		String itemTypeName = forOfDef.itemType;
		String listName = forOfDef.listName;
		String origListName = listName;

		CmpVar cmpVar = cmpVars.peek();

		if ("let".equals(itemTypeName) || "var".equals(itemTypeName)) {
			ClassDef[] lastClassDef = new ClassDef[1];
			listName = prefixName(listName, cmpVar, lastClassDef);
			ClassDef listClass = lastClassDef[0];
			if (!listClass.valid()) {
				throw new NgoyException("'%s' is not iterable. Must be an instance of %s, %s, or an array", origListName, Iterable.class.getName(), Stream.class.getName());
			}
			itemTypeName = sourceClassName(listClass.getItemType());
		} else {
			listName = prefixName(listName);
		}

		Set<String> ex = new HashSet<>(asList(itemName));

		String itemTypeClazz = primitiveToRefType(itemTypeName);

		String iterVar = createLocalVar("iter");
		printExprComment(origListName);
		$("for (final ", IteratorWithVariables.class, " ", iterVar, "=new ", IteratorWithVariables.class, "(", listName, "); ", iterVar, ".hasNext();){");
		$(itemTypeClazz, " ", itemName, "=(", itemTypeClazz, ")", iterVar, ".next();");

		Set<Entry<ForOfVariable, String>> entries = variables.entrySet();
		for (Map.Entry<ForOfVariable, String> e : entries) {
			String varType = e.getKey() == ForOfVariable.index ? "int" : "boolean";
			$(varType, " ", e.getValue(), "=", iterVar, ".", e.getKey(), ";");
			ex.add(e.getValue());
		}

		prefixExcludes.push(ex);

		Map<String, Class<?>> iterVarDef = new HashMap<>();
		iterVarDef.put(itemName, tryLoadClass(itemTypeName));
		localVarDefs.push(iterVarDef);
	}

	private void flushOut() {
		out.flush();
	}

	private void printOutExpr(String s) {
		out.print(s, true, false);
	}

	private String[] getExprCommentPrint(String expr) {
		String debugInfo = format("%s:::%s", sourcePosition, NEWLINE_PATTERN.matcher(expr)
				.replaceAll(""));

		return new String[] { format("%s=\"%s\"", lastExprVar, escapeJava(debugInfo)), debugInfo };
	}

	private void printExprComment(String expr) {
		out.flush();

		String[] exprComment = getExprCommentPrint(expr);
		// this is for runtime errors
		$(exprComment[0], ";");

		// this is for compile time errors
		$(EXPR_COMMENT_TAG, exprComment[1]);
	}

	private void printOut(Object... s) {
		String text = Stream.of(s)
				.map(Object::toString)
				.map(Util::escapeJava)
				.collect(joining(""));
		out.print(text, false, false);
	}

	private void ifExprIsTrue(String expr) {
		flushOut();
		String exprComment = getExprCommentPrint(expr)[0];
		$("if((", exprComment, ")!=null&&(", prefixName(expr), ")){");
	}

	@Override
	public void componentStartInput(CmpRef cmpRef, boolean appRoot, List<CmpInput> params) {
		Class<?> cmpClass = cmpRef.clazz;

		cmpVar = createLocalVar(cmpRef.clazz.getSimpleName());
		if (appRoot) {
			$("final ", cmpClass, " ", cmpVar, "=(", cmpClass, ")", CMP_INSTANCE_VAR, ";");
		} else {
			$("final ", cmpClass, " ", cmpVar, "=(", cmpClass, ")", CTX_VAR, ".cmpNew(", cmpClass, ".class);");
		}
		$("{");

		// testForOfNested2
		cmpVars.push(new CmpVar(cmpVar, cmpRef.clazz));
		setInputs(params);
		cmpVars.pop();
	}

	@Override
	public void componentStart(CmpRef cmpRef) {
		cmpVars.push(new CmpVar(cmpVar, cmpRef.clazz));
		if (OnInit.class.isAssignableFrom(cmpRef.clazz)) {
			$("((", OnInit.class, ")", cmpVar, ").onInit();");
		}
	}

	@Override
	public void componentContentStart(CmpRef cmpRef) {
		flushOut();
		if (OnRender.class.isAssignableFrom(cmpRef.clazz)) {
			$("((", OnRender.class, ")", cmpVar, ").onRender(", CTX_VAR, ");");
		}
	}

	@Override
	public void componentEnd() {
		flushOut();

		CmpVar cmpVar = cmpVars.pop();

		if (OnRender.class.isAssignableFrom(cmpVar.cmpClass)) {
			$("((", OnRender.class, ")", cmpVar.name, ").onRenderEnd(", CTX_VAR, ");");
		}

		if (OnDestroy.class.isAssignableFrom(cmpVar.cmpClass)) {
			$("((", OnDestroy.class, ")", cmpVar.name, ").onDestroy();");
		}

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
		$("final ", StringBuilder.class, " ", listVar, "=new ", StringBuilder.class, "();");
		for (String[] pair : exprPairs) {
			String clazz = pair[0];
			String expr = pair[1];
			if (expr.isEmpty()) {
				$("if(", listVar, ".length()>0){", listVar, ".append(\"", delimiter, "\");}");
				$(listVar, ".append(\"", clazz, "\");");
			} else {
				String ex = prefixName(expr);
				if (forStyles) {
					if (clazz.equals("ngStyle")) {
						$("for (", Map.class, ".Entry entry : ", ex, ".entrySet()){");
						String valueVar = createLocalVar("styleValue");
						$("final Object ", valueVar, "=", "entry.getValue();");
						$("  if(", valueVar, "!= null && !", valueVar, ".toString().isEmpty()){");
						$("if(", listVar, ".length()>0){", listVar, ".append(\"", delimiter, "\");}");
						$(listVar, ".append(entry.getKey()).append(':').append(", valueVar, ");");
						$("  }");
						$("}");
					} else {
						String[] classAndUnit = parseUnit(clazz);
						String unit = classAndUnit[1];
						clazz = classAndUnit[0];

						String exVar = createLocalVar("expr");
						printExprComment(ex);
						$("final Object ", exVar, "=", ex, ";");
						$("if(", exVar, "!=null && !", exVar, ".toString().isEmpty()){");
						$("if(", listVar, ".length()>0){", listVar, ".append(\"", delimiter, "\");}");
						$(listVar, ".append(\"", clazz, "\").append(':').append(", exVar, ");");
						if (!unit.isEmpty()) {
							$(listVar, ".append(\"", unit, "\");");
						}
						$("}");
					}
				} else {
					if (clazz.equals("ngClass")) {
						$("for (", Map.class, ".Entry entry : ", ex, ".entrySet()){");
						$("  if((Boolean)entry.getValue()){");
						$("if(", listVar, ".length()>0){", listVar, ".append(\"", delimiter, "\");}");
						$(listVar, ".append(entry.getKey());");
						$("  }");
						$("}");
					} else {
						printExprComment(ex);
						$("if(", ex, "){");
						$("if(", listVar, ".length()>0){", listVar, ".append(\"", delimiter, "\");}");
						$(listVar, ".append(\"", clazz, "\");");
						$("}");
					}
				}
			}
		}

		$("if(", listVar, ".length()>0){");
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

			CmpVar parentCmp = cmpVars.get(1);
			printExprComment(input.value);
			String value = inputValueType == ValueType.EXPR ? prefixName(input.value, parentCmp, null) : format("\"%s\"", Util.escapeJava(input.value));
			switch (inputType) {
			case FIELD:
				$(cmpVars.peek().name, ".", input.input, "=", value, ";");
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
