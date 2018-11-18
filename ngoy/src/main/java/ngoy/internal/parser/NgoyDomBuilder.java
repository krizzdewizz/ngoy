package ngoy.internal.parser;

import jodd.lagarto.LagartoParser;
import jodd.lagarto.Tag;
import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.Element;
import jodd.lagarto.dom.LagartoDOMBuilder;
import jodd.lagarto.dom.LagartoDOMBuilderTagVisitor;

public class NgoyDomBuilder extends LagartoDOMBuilder {

	public NgoyDomBuilder() {
		config //
				.setCalculatePosition(true)
				.setCaseSensitive(true);
	}

	@Override
	protected Document doParse(LagartoParser parser) {
		parser.setConfig(config);

		LagartoDOMBuilderTagVisitor builder = new LagartoDOMBuilderTagVisitor(this) {
			@Override
			protected Element createElementNode(Tag tag) {
				Element el = super.createElementNode(tag);
				return new NgoyElement(rootNode, tag, el.isVoidElement(), el.isSelfClosed(), tag.getPosition());
			}
		};

		parser.parse(builder);

		return builder.getDocument();
	}
}