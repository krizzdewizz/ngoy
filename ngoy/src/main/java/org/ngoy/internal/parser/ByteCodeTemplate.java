package org.ngoy.internal.parser;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.cojen.classfile.CodeBuilder;
import org.cojen.classfile.Label;
import org.cojen.classfile.LocalVariable;
import org.cojen.classfile.Modifiers;
import org.cojen.classfile.RuntimeClassFile;
import org.cojen.classfile.TypeDesc;
import org.ngoy.core.NgoyException;
import org.ngoy.core.internal.Ctx;

public class ByteCodeTemplate implements ParserHandler {

	private class Out extends BufferedOutput {
		@Override
		protected void doPrint(String text, boolean isExpr) {
			if (isExpr) {
				throw new NgoyException("Must not be called for expressions. Please contact developer.");
			}

			printOut(text);
		}

		private void printOut(String string) {
			run.loadLocal(ctxParam);
			run.loadConstant(string);
			run.invokeVirtual(ctxType, "print", null, singleObjectParamType);
		}

		void printOutExpr(String text, List<String[]> pipes) {
			flush();
			LocalVariable pipesArray;
			if (pipes.isEmpty()) {
				pipesArray = emptyExprParams;
			} else {
				List<LocalVariable> ps = new ArrayList<>();
				for (String[] pipe : pipes) {
					ps.add(toArray(asList(pipe)));
				}
				pipesArray = toStringTable(ps);
			}

			run.loadLocal(ctxParam);
			run.loadLocal(ctxParam);
			run.loadConstant(text);
			run.loadLocal(pipesArray);

			run.invokeVirtual(ctxType, "eval", TypeDesc.OBJECT, evalParamTypes);
			run.invokeVirtual(ctxType, "printEscaped", null, singleObjectParamType);
		}
	}

	private static class Repeat {
		Label label1;
		Label label2;
		LocalVariable iter;

		Repeat(Label label1, Label label2, LocalVariable iter) {
			this.label1 = label1;
			this.label2 = label2;
			this.iter = iter;
		}
	}

	private static final TypeDesc ctxType = TypeDesc.forClass(Ctx.class);
	private static final TypeDesc iterableType = TypeDesc.forClass(Iterable.class);
	private static final TypeDesc iteratorType = TypeDesc.forClass(Iterator.class);
	private static final TypeDesc stringArrayType = TypeDesc.STRING.toArrayType();
	private static final TypeDesc stringTableType = stringArrayType.toArrayType();
	private static final TypeDesc[] pushCmpContextParamTypes = new TypeDesc[] { TypeDesc.STRING, stringArrayType };
	private static final TypeDesc[] evalParamTypes = new TypeDesc[] { TypeDesc.STRING, stringTableType };
	private static final TypeDesc[] forOfStartParamTypes = new TypeDesc[] { TypeDesc.STRING, stringArrayType };
	private static final TypeDesc[] evalClassesParamTypes = new TypeDesc[] { stringTableType };
	private static final TypeDesc[] pushForOfContextParamsTypes = new TypeDesc[] { TypeDesc.STRING, TypeDesc.OBJECT };
	private static final TypeDesc[] singleStringParamType = new TypeDesc[] { TypeDesc.STRING };
	private static final TypeDesc[] singleObjectParamType = new TypeDesc[] { TypeDesc.OBJECT };
	private static final TypeDesc[] runParamTypes = new TypeDesc[] { ctxType };

	private final LinkedList<Label> elementConditionals = new LinkedList<>();
	private final LinkedList<Repeat> elementRepeated = new LinkedList<>();
	private final Out out = new Out();
	private final String className;
	private final String contentType;

	private CodeBuilder run;
	private RuntimeClassFile classFile;
	private LocalVariable ctxParam;
	private LocalVariable emptyExprParams;
	private LocalVariable emptyStringArray;

	public ByteCodeTemplate(String className, String contentType) {
		this.className = className;
		this.contentType = contentType;
	}

	@Override
	public void documentStart() {
		classFile = new RuntimeClassFile(className, Object.class.getName(), Thread.currentThread()
				.getContextClassLoader());
		run = new CodeBuilder(classFile.addMethod(Modifiers.PUBLIC_STATIC, "render", null, runParamTypes));
		ctxParam = run.getParameter(0);

		// create empty pipes array
		emptyExprParams = run.createLocalVariable(stringTableType);
		run.loadConstant(0);
		run.newObject(stringTableType);
		run.storeLocal(emptyExprParams);

		emptyStringArray = run.createLocalVariable(stringArrayType);
		run.loadConstant(0);
		run.newObject(stringArrayType);
		run.storeLocal(emptyStringArray);
	}

	@Override
	public void documentEnd() {
		flushOut();
		run.returnVoid();
	}

	@Override
	public void text(String text, boolean textIsExpr, List<String[]> pipes) {
		if (text.isEmpty()) {
			return;
		}
		if (textIsExpr) {
			out.printOutExpr(text, pipes);
		} else {
			out.print(text, false, true, contentType);
		}
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
		flushOut();

		List<LocalVariable> ps = new ArrayList<>();
		for (String[] pipe : classExprPairs) {
			ps.add(toArray(asList(pipe)));
		}
		LocalVariable classesArray = toStringTable(ps);

		run.loadLocal(ctxParam);
		run.loadLocal(classesArray);
		run.invokeVirtual(ctxType, "evalClasses", TypeDesc.STRING, evalClassesParamTypes);
		LocalVariable evalResultVar = run.createLocalVariable(null, TypeDesc.OBJECT);
		run.storeLocal(evalResultVar);

		run.loadLocal(evalResultVar);
		Label ifNull = run.createLabel();
		run.ifNullBranch(ifNull, true);

		printOut(" class=\"");

		flushOut();
		run.loadLocal(ctxParam);
		run.loadLocal(evalResultVar);
		run.invokeVirtual(ctxType, "print", null, singleObjectParamType);

		printOut("\"");

		flushOut();
		ifNull.setLocation();
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
		run.loadLocal(ctxParam);
		run.loadConstant(expr);
		run.loadLocal(emptyExprParams);
		run.invokeVirtual(ctxType, "eval", TypeDesc.OBJECT, evalParamTypes);
		LocalVariable evalResultVar = run.createLocalVariable(null, TypeDesc.OBJECT);
		run.storeLocal(evalResultVar);

		run.loadLocal(evalResultVar);
		Label ifNull = run.createLabel();
		run.ifNullBranch(ifNull, true);

		printOut(" ", name, "=\"");

		flushOut();
		run.loadLocal(ctxParam);
		run.loadLocal(evalResultVar);
		run.invokeVirtual(ctxType, "printEscaped", null, singleObjectParamType);

		printOut("\"");

		flushOut();
		ifNull.setLocation();
	}

	@Override
	public void attributeEnd() {
		printOut("\"");
	}

	@Override
	public void elementEnd(String name) {
		printOut("</", name, ">");
	}

	private Label ifExprIsTrue(String expr) {
		flushOut();
		run.loadLocal(ctxParam);
		run.loadConstant(expr);
		run.invokeVirtual(ctxType, "evalBool", TypeDesc.BOOLEAN, singleStringParamType);
		Label ifTrue = run.createLabel();
		run.ifZeroComparisonBranch(ifTrue, "==");
		return ifTrue;
	}

	@Override
	public void elementConditionalStart(String expr) {
		flushOut();
		elementConditionals.push(ifExprIsTrue(expr));
	}

	@Override
	public void elementConditionalElse() {
		flushOut();
		Label ifFalse = run.createLabel();
		run.branch(ifFalse);

		elementConditionals.pop()
				.setLocation();

		elementConditionals.push(ifFalse);
	}

	@Override
	public void elementConditionalEnd() {
		flushOut();
		elementConditionals.pop()
				.setLocation();
	}

	@Override
	public void elementRepeatedEnd() {
		flushOut();
		run.loadLocal(ctxParam);
		run.invokeVirtual(ctxType, "popContext", null, null);

		Repeat rep = elementRepeated.pop();

		rep.label1.setLocation();
		run.loadLocal(rep.iter);
		run.invokeInterface(iteratorType, "hasNext", TypeDesc.BOOLEAN, null);
		run.ifZeroComparisonBranch(rep.label2, "!=");

		run.loadLocal(ctxParam);
		run.invokeVirtual(ctxType, "forOfEnd", null, null);
	}

	public void elementRepeatedStart(String[] itemAndListName, Map<ForOfVariable, String> variables) {
		flushOut();

		String listName = itemAndListName[1];

		LocalVariable arr;

		if (variables.isEmpty()) {
			arr = emptyStringArray;
		} else {
			arr = run.createLocalVariable(stringArrayType);
			run.loadConstant(2 * variables.size());
			run.newObject(stringArrayType);
			run.storeLocal(arr);
		}

		int i = 0;
		Set<Entry<ForOfVariable, String>> entries = variables.entrySet();
		for (Map.Entry<ForOfVariable, String> e : entries) {
			run.loadLocal(arr);
			run.loadConstant(i);
			run.loadConstant(e.getKey()
					.name());
			run.storeToArray(TypeDesc.OBJECT);

			run.loadLocal(arr);
			run.loadConstant(i + 1);
			run.loadConstant(e.getValue());
			run.storeToArray(TypeDesc.OBJECT);
			i += 2;
		}

		run.loadLocal(ctxParam);
		run.loadConstant(listName);
		run.loadLocal(arr);
		run.invokeVirtual(ctxType, "forOfStart", iterableType, forOfStartParamTypes);

		run.invokeInterface(iterableType, "iterator", iteratorType, null);

		LocalVariable iter = run.createLocalVariable(null, TypeDesc.OBJECT);
		run.storeLocal(iter);
		Label label1 = run.createLabel();
		run.branch(label1);
		Label label2 = run.createLabel();
		label2.setLocation();
		run.loadLocal(iter);
		run.invokeInterface(iteratorType, "next", TypeDesc.OBJECT, null);
		LocalVariable item = run.createLocalVariable(null, TypeDesc.OBJECT);
		run.storeLocal(item);

		String itemName = itemAndListName[0];
		run.loadLocal(ctxParam);
		run.loadConstant(itemName);
		run.loadLocal(item);
		run.invokeVirtual(ctxType, "pushForOfContext", null, pushForOfContextParamsTypes);

		elementRepeated.push(new Repeat(label1, label2, iter));
	}

	private void printOut(String... s) {
		out.print(Stream.of(s)
				.collect(joining()), false, false, contentType);
	}

	/**
	 * Flushes must occur before 'non-print-text code' like if, else, method calls
	 * etc.
	 */
	private void flushOut() {
		out.flush();
	}

	public RuntimeClassFile getClassFile() {
		return classFile;
	}

	@Override
	public void componentStart(String clazz, List<String> params) {
		LocalVariable paramsArray = toArray(params);
		run.loadLocal(ctxParam);
		run.loadConstant(clazz);
		run.loadLocal(paramsArray);
		run.invokeVirtual(ctxType, "pushCmpContext", null, pushCmpContextParamTypes);
	}

	@Override
	public void componentEnd() {
		flushOut();
		run.loadLocal(ctxParam);
		run.invokeVirtual(ctxType, "popCmpContext", null, null);
	}

	private LocalVariable toArray(List<String> list) {
		LocalVariable arr = run.createLocalVariable(stringArrayType);
		run.loadConstant(list.size());
		run.newObject(stringArrayType);
		run.storeLocal(arr);

		for (int i = 0, n = list.size(); i < n; i++) {
			run.loadLocal(arr);
			run.loadConstant(i);
			run.loadConstant(list.get(i));
			run.storeToArray(TypeDesc.OBJECT);
		}
		return arr;
	}

	private LocalVariable toStringTable(List<LocalVariable> list) {
		LocalVariable arr = run.createLocalVariable(stringTableType);
		run.loadConstant(list.size());
		run.newObject(stringTableType);
		run.storeLocal(arr);

		for (int i = 0, n = list.size(); i < n; i++) {
			run.loadLocal(arr);
			run.loadConstant(i);
			run.loadLocal(list.get(i));
			run.storeToArray(TypeDesc.OBJECT);
		}
		return arr;
	}

	@Override
	public void ngContentStart() {
		flushOut();

		run.loadLocal(ctxParam);
		run.invokeVirtual(ctxType, "pushParentContext", null, null);
	}

	@Override
	public void ngContentEnd() {
		flushOut();

		run.loadLocal(ctxParam);
		run.invokeVirtual(ctxType, "popContext", null, null);
	}
}
