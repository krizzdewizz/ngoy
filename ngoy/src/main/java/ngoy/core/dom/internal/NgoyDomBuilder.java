package ngoy.core.dom.internal;

import jodd.lagarto.LagartoParser;
import jodd.lagarto.Tag;
import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.Element;
import jodd.lagarto.dom.LagartoDOMBuilder;
import jodd.lagarto.dom.LagartoDOMBuilderTagVisitor;
import ngoy.core.dom.NgoyElement;

public class NgoyDomBuilder extends LagartoDOMBuilder {

	private final int baseLineNumber;

	public NgoyDomBuilder(int baseLineNumber) {
		this.baseLineNumber = baseLineNumber;
		config //
				.setCalculatePosition(true)
				.setCaseSensitive(true)
				.setParseXmlTags(true);
	}

	@Override
	protected Document doParse(LagartoParser parser) {
		parser.setConfig(config);

		LagartoDOMBuilderTagVisitor builder = new LagartoDOMBuilderTagVisitor(this) {
			@Override
			protected Element createElementNode(Tag tag) {
				Element el = super.createElementNode(tag);
				return new NgoyElement(rootNode, tag, el.isVoidElement(), el.isSelfClosed(), tag.getPosition(), baseLineNumber);
			}
		};

		parser.parse(builder);

		return builder.getDocument();
	}
}