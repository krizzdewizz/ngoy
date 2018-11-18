package ngoy.internal.parser;

import jodd.jerry.Jerry;
import jodd.lagarto.Tag;
import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.Element;

public class NgoyElement extends Element {

	public static String getPosition(Jerry el) {
		return ((NgoyElement) el.get(0)).getPosition();
	}

	public static void setNodeName(Jerry el, String name) {
		((NgoyElement) el.get(0)).setNodeName(name);
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

	private void setNodeName(String nodeName) {
		this.writableNodeName = nodeName;
	}

	private String getPosition() {
		return position;
	}
}
