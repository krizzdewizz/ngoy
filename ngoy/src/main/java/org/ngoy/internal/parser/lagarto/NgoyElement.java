package org.ngoy.internal.parser.lagarto;

import jodd.jerry.Jerry;
import jodd.lagarto.Tag;
import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.Element;

public class NgoyElement extends Element {

	public static NgoyElement get(Jerry el) {
		return (NgoyElement) el.get(0);
	}

	private final String position;
	private String writableNodeName;

	public NgoyElement(Document ownerDocument, String name) {
		super(ownerDocument, name);
		writableNodeName = name;
		position = "<position unknown>";
	}

	public NgoyElement(Document ownerNode, Tag tag, boolean voidElement, boolean selfClosed, String position) {
		super(ownerNode, tag, voidElement, selfClosed);
		this.position = position;
		writableNodeName = String.valueOf(tag.getName());
	}

	@Override
	public String getNodeName() {
		return writableNodeName;
	}

	public void setNodeName(String nodeName) {
		this.writableNodeName = nodeName;
	}

	public String getPosition() {
		return position;
	}
}
