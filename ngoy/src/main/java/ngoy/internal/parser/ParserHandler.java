package ngoy.internal.parser;

import java.util.List;
import java.util.Map;

import ngoy.core.internal.CmpRef;
import ngoy.internal.parser.Inputs.CmpInput;

public interface ParserHandler {
	void documentStart(List<Class<?>> pipes);

	void documentEnd();

	void elementHead(String name);

	void attributeStart(String name, boolean hasValue);

	void attributeClasses(List<String[]> classExprPairs);

	void attributeStyles(List<String[]> styleExprPairs);

	void attributeExpr(String name, String expr);

	void attributeEnd();

	void elementHeadEnd();

	void text(String text, boolean textIsExpr, boolean escape);

	void elementEnd(String name);

	void elementConditionalStart(String expr, String switchFirstCase);

	void elementConditionalElseIf(String expr);

	void elementConditionalElse();

	void elementConditionalEnd();

	void elementRepeatedStart(String[] itemAndListName, Map<ForOfVariable, String> variables);

	void elementRepeatedEnd();

	void componentStart(CmpRef cmpRef);

	void componentEnd();

	void ngContentStart();

	void ngContentEnd();

	void textOverride(String expr);

	void componentStartInput(CmpRef cmpRef, boolean appRoot, List<CmpInput> params);

	class Default implements ParserHandler {

		@Override
		public void documentStart(List<Class<?>> pipes) {
		}

		@Override
		public void documentEnd() {
		}

		@Override
		public void elementHead(String name) {
		}

		@Override
		public void attributeStart(String name, boolean hasValue) {
		}

		@Override
		public void attributeClasses(List<String[]> classExprPairs) {
		}

		@Override
		public void attributeExpr(String name, String expr) {
		}

		@Override
		public void attributeEnd() {
		}

		@Override
		public void elementHeadEnd() {
		}

		@Override
		public void text(String text, boolean textIsExpr, boolean escape) {
		}

		@Override
		public void elementEnd(String name) {
		}

		@Override
		public void elementConditionalStart(String expr, String switchFirstCase) {
		}

		@Override
		public void elementConditionalElse() {
		}

		@Override
		public void elementConditionalElseIf(String expr) {
		}

		@Override
		public void elementConditionalEnd() {
		}

		@Override
		public void elementRepeatedStart(String[] itemAndListName, Map<ForOfVariable, String> variables) {
		}

		@Override
		public void elementRepeatedEnd() {
		}

		@Override
		public void componentStart(CmpRef cmpRef) {
		}

		@Override
		public void componentEnd() {
		}

		@Override
		public void ngContentStart() {
		}

		@Override
		public void ngContentEnd() {
		}

		@Override
		public void textOverride(String expr) {
		}

		@Override
		public void attributeStyles(List<String[]> styleExprPairs) {
		}

		@Override
		public void componentStartInput(CmpRef cmpRef, boolean appRoot, List<CmpInput> params) {
		}
	}

	class Delegate implements ParserHandler {
		private final ParserHandler target;

		public Delegate(ParserHandler target) {
			this.target = target;
		}

		public void documentStart(List<Class<?>> pipes) {
			target.documentStart(pipes);
		}

		public void documentEnd() {
			target.documentEnd();
		}

		public void elementHead(String name) {
			target.elementHead(name);
		}

		public void attributeStart(String name, boolean hasValue) {
			target.attributeStart(name, hasValue);
		}

		public void attributeClasses(List<String[]> classExprPairs) {
			target.attributeClasses(classExprPairs);
		}

		public void attributeExpr(String name, String expr) {
			target.attributeExpr(name, expr);
		}

		public void attributeEnd() {
			target.attributeEnd();
		}

		public void elementHeadEnd() {
			target.elementHeadEnd();
		}

		public void text(String text, boolean textIsExpr, boolean escape) {
			target.text(text, textIsExpr, escape);
		}

		public void elementEnd(String name) {
			target.elementEnd(name);
		}

		public void elementConditionalStart(String expr, String switchFirstCase) {
			target.elementConditionalStart(expr, switchFirstCase);
		}

		public void elementConditionalElse() {
			target.elementConditionalElse();
		}

		@Override
		public void elementConditionalElseIf(String expr) {
			target.elementConditionalElseIf(expr);
		}

		public void elementConditionalEnd() {
			target.elementConditionalEnd();
		}

		public void elementRepeatedStart(String[] itemAndListName, Map<ForOfVariable, String> variables) {
			target.elementRepeatedStart(itemAndListName, variables);
		}

		public void elementRepeatedEnd() {
			target.elementRepeatedEnd();
		}

		public void componentStart(CmpRef cmpRef) {
			target.componentStart(cmpRef);
		}

		public void componentStartInput(CmpRef cmpRef, boolean appRoot, List<CmpInput> params) {
			target.componentStartInput(cmpRef, appRoot, params);
		}

		public void componentEnd() {
			target.componentEnd();
		}

		public void ngContentStart() {
			target.ngContentStart();
		}

		public void ngContentEnd() {
			target.ngContentEnd();
		}

		@Override
		public void textOverride(String expr) {
			target.textOverride(expr);
		}

		@Override
		public void attributeStyles(List<String[]> styleExprPairs) {
			target.attributeStyles(styleExprPairs);
		}
	}
}
