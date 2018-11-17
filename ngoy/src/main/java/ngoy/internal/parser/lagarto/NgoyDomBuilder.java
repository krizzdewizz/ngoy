package ngoy.internal.parser.lagarto;

import jodd.lagarto.LagartoParser;
import jodd.lagarto.Tag;
import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.Element;
import jodd.lagarto.dom.LagartoDOMBuilder;
import jodd.lagarto.dom.LagartoDOMBuilderTagVisitor;

public class NgoyDomBuilder extends LagartoDOMBuilder {

	private static LagartoDOMBuilderTagVisitor domBuilderTagVisitor;

	public NgoyDomBuilder() {
		config = config //
				.setCalculatePosition(true)
				.setCaseSensitive(true);
	}

	@Override
	protected Document doParse(LagartoParser lagartoParser) {
		lagartoParser.setConfig(config);

		domBuilderTagVisitor = new LagartoDOMBuilderTagVisitor(this) {
			@Override
			protected Element createElementNode(Tag tag) {
				Element el = super.createElementNode(tag);
				return new NgoyElement(rootNode, tag, el.isVoidElement(), el.isSelfClosed(), tag.getPosition());
			}
		};

		lagartoParser.parse(domBuilderTagVisitor);

		return domBuilderTagVisitor.getDocument();
	}
}