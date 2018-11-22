package ngoy.core.dom;

import static java.lang.String.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jodd.jerry.Jerry;
import jodd.lagarto.Tag;
import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.Element;

public class NgoyElement extends Element {

	public static class Position {

		private static final Pattern POSITION_PATTERN = Pattern.compile("\\[(\\d*):(\\d*) @(.*)\\]");

		private static Position parse(String position, int baseLineNumber) {
			Matcher matcher = POSITION_PATTERN.matcher(position);
			if (matcher.find()) {
				return new Position( //
						Integer.parseInt(matcher.group(1)) + baseLineNumber, //
						Integer.parseInt(matcher.group(2)), //
						Integer.parseInt(matcher.group(3)));
			}

			return new Position(1, 1, 0);
		}

		private final int line;
		private final int col;
		private final int pos;

		public Position(int line, int col, int pos) {
			this.line = line;
			this.col = col;
			this.pos = pos;
		}

		public int getLine() {
			return line;
		}

		public int getCol() {
			return col;
		}

		public int getPos() {
			return pos;
		}

		@Override
		public String toString() {
			return format("[%s:%s @%s]", line, col, pos);
		}
	}

	public static Position getPosition(Jerry el) {
		return ((NgoyElement) el.get(0)).getPosition();
	}

	public static void setNodeName(Jerry el, String name) {
		((NgoyElement) el.get(0)).setNodeName(name);
	}

	private final Position position;
	private String writableNodeName;

	public NgoyElement(Document ownerDocument, String name) {
		super(ownerDocument, name);
		writableNodeName = name;
		position = new Position(0, 0, 0);
	}

	public NgoyElement(Document ownerNode, Tag tag, boolean voidElement, boolean selfClosed, String position, int baseLineNumber) {
		super(ownerNode, tag, voidElement, selfClosed);
		this.position = Position.parse(position, baseLineNumber);
		writableNodeName = String.valueOf(tag.getName());
	}

	@Override
	public String getNodeName() {
		return writableNodeName;
	}

	private void setNodeName(String nodeName) {
		this.writableNodeName = nodeName;
	}

	private Position getPosition() {
		return position;
	}
}
