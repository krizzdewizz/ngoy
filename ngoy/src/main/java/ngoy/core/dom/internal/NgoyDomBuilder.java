package ngoy.core.dom.internal;

import jodd.lagarto.LagartoParser;
import jodd.lagarto.Tag;
import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.Element;
import jodd.lagarto.dom.LagartoDOMBuilder;
import jodd.lagarto.dom.LagartoDOMBuilderTagVisitor;
import ngoy.core.dom.NgoyElement;

public class NgoyDomBuilder extends LagartoDOMBuilder {

	private final LagartoDOMBuilderTagVisitor visitor;

	public NgoyDomBuilder(int baseLineNumber) {
		config //
				.setCalculatePosition(true)
				.setCaseSensitive(true)
				.setParseXmlTags(true);

		visitor = new LagartoDOMBuilderTagVisitor(this) {
			@Override
			protected Element createElementNode(Tag tag) {

				boolean hasVoidTags = htmlVoidRules != null;

				boolean isVoid = false;
				boolean selfClosed = false;

				if (hasVoidTags) {
					isVoid = htmlVoidRules.isVoidTag(tag.getName());

					// HTML and XHTML
					if (isVoid) {
						// it's void tag, lookup the flag
						selfClosed = config.isSelfCloseVoidTags();
					}
				} else {
					// XML, no voids, lookup the flag
					selfClosed = config.isSelfCloseVoidTags();
				}
				return new NgoyElement(rootNode, tag, isVoid, selfClosed, tag.getPosition(), baseLineNumber);
			}
		};
	}

	@Override
	protected Document doParse(LagartoParser parser) {
		parser.setConfig(config);
		parser.parse(visitor);
		return visitor.getDocument();
	}
}