package org.ngoy.internal.parser;

import java.util.List;

public interface ParserHandler {
	void documentStart();

	void documentEnd();

	void elementHead(String name);

	void attributeStart(String name, boolean hasValue);

	void attributeClasses(List<String[]> classExprPairs);

	void attributeExpr(String name, String expr);

	void attributeEnd();

	void elementHeadEnd();

	void text(String text, boolean textIsExpr, List<String[]> pipes);

	void elementEnd(String name);

	void elementConditionalStart(String expr);

	void elementConditionalElse();

	void elementConditionalEnd();

	void elementRepeatedStart(String expr);

	void elementRepeatedEnd();

	void componentStart(String clazz, List<String> params);

	void componentEnd();

	void ngContentStart();

	void ngContentEnd();
}
