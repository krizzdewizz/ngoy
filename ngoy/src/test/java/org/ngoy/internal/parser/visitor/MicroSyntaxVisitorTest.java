package org.ngoy.internal.parser.visitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.hamcrest.CoreMatchers.containsString;
import static org.ngoy.internal.parser.visitor.MicroSyntaxVisitor.parseVariables;

import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ngoy.core.NgoyException;
import org.ngoy.internal.parser.ForOfVariable;
import org.ngoy.internal.parser.Parser;

public class MicroSyntaxVisitorTest {

	private MicroSyntaxVisitor visitor;

	@Before
	public void beforeEach() {
		visitor = new MicroSyntaxVisitor(new NodeVisitor() {

			@Override
			public void tail(Node node, int depth) {
			}

			@Override
			public void head(Node node, int depth) {
			}
		});
	}

	@Test
	public void test() {
		Parser parser = new Parser();

		List<Node> nodes = parser.parse("<div *ngIf=\"true\">xx</div>", false);

		nodes.forEach(n -> n.traverse(visitor));
		assertThat(nodes).hasSize(1);
		Element el = (Element) nodes.get(0);
		assertThat(el.nodeName()).isEqualTo("ng-template");
		assertThat(el.attr("[ngIf]")).isEqualTo("true");
	}

	@Test
	public void testElse() {
		Parser parser = new Parser();

		List<Node> nodes = parser.parse("<div *ngIf=\"true ; else qbert\">xx</div>", false);

		nodes.forEach(n -> n.traverse(visitor));
		assertThat(nodes).hasSize(1);
		Element el = (Element) nodes.get(0);
		assertThat(el.toString()).isEqualTo("<ng-template [ngIf]=\"true\" ngElse=\"qbert\">\n" + //
				" <div>\n" + //
				"  xx\n" + //
				" </div>\n" + //
				"</ng-template>");
	}

	@Test
	public void testFor() {
		Parser parser = new Parser();

		List<Node> nodes = parser.parse("<div *ngFor=\"let p of persons; index as i; first as f; last as l; even as e; odd as o\">xx</div>", false);

		nodes.forEach(n -> n.traverse(visitor));
		Element el = (Element) nodes.get(0);
		assertThat(el.toString()).isEqualTo("<ng-template ngFor let-p [ngForOf]=\"persons\" let-i=\"index\" let-f=\"first\" let-l=\"last\" let-e=\"even\" let-o=\"odd\">\n" + //
				" <div>\n" + //
				"  xx\n" + //
				" </div>\n" + //
				"</ng-template>");
	}

	@Test
	public void testNone() {
		assertThat(parseVariables("let p of persons;")).isEmpty();
	}

	@Test
	public void testIndex() {
		Map<ForOfVariable, String> map = parseVariables("let p of persons;  index as ii ");
		assertThat(map).size()
				.isEqualTo(1);
		assertThat(map).contains(entry(ForOfVariable.index, "ii"));
	}

	@Test
	public void testIndexOdd() {
		Map<ForOfVariable, String> map = parseVariables("let p of persons;  index as ii ; odd as o");
		assertThat(map).size()
				.isEqualTo(2);
		assertThat(map).contains(entry(ForOfVariable.index, "ii"));
		assertThat(map).contains(entry(ForOfVariable.odd, "o"));
	}

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void testUnknownVariable() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Unknown ngFor variable"));
		expectedEx.expectMessage(containsString("unk"));
		parseVariables("let p of persons;  unk as ii");
	}

	@Test
	public void testParseError() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Parse error in ngFor"));
		parseVariables("let p of persons;  index = ii");
	}
}
