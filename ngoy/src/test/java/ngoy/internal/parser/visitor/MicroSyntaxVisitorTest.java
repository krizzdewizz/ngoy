package ngoy.internal.parser.visitor;

import static ngoy.core.XDom.traverse;
import static ngoy.internal.parser.visitor.MicroSyntaxVisitor.parseVariables;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Element;
import ngoy.core.NgoyException;
import ngoy.core.XDom.NodeVisitor;
import ngoy.internal.parser.ForOfVariable;
import ngoy.internal.parser.Parser;

public class MicroSyntaxVisitorTest {

	private MicroSyntaxVisitor visitor;
	private Parser parser;

	@Before
	public void beforeEach() {
		parser = new Parser();
		visitor = new MicroSyntaxVisitor(new NodeVisitor.Default());
	}

	@Test
	public void test() {

		Jerry nodes = parser.parse("<div *ngIf=\"true\">xx</div>");

		traverse(nodes, visitor);
		assertThat(nodes).hasSize(1);
		Element el = (Element) nodes.get(0)
				.getChild(0);
		assertThat(el.getNodeName()).isEqualTo("ng-template");
		assertThat(el.getAttribute("[ngIf]")).isEqualTo("true");
	}

	@Test
	public void testElse() {
		Jerry nodes = parser.parse("<div *ngIf=\"true ; else qbert\">xx</div>");

		traverse(nodes, visitor);
		assertThat(nodes).hasSize(1);
		assertThat(nodes.get(0)
				.getHtml()).isEqualTo("<ng-template [ngIf]=\"true\" ngElse=\"qbert\"><div>xx</div></ng-template>");
	}

	@Test
	public void testSwitchCase() {
		Jerry nodes = parser.parse("<div *ngSwitchCase=\"'happy'\">HAPPY</div>");

		traverse(nodes, visitor);
		assertThat(nodes).hasSize(1);
		assertThat(nodes.get(0)
				.getHtml()).isEqualTo("<ng-template [ngSwitchCase]=\"'happy'\"><div>HAPPY</div></ng-template>");
	}

	@Test
	public void testSwitchDefault() {
		Jerry nodes = parser.parse("<div *ngSwitchDefault>HAPPY</div>");

		traverse(nodes, visitor);
		assertThat(nodes).hasSize(1);
		assertThat(nodes.get(0)
				.getHtml()).isEqualTo("<ng-template ngSwitchDefault><div>HAPPY</div></ng-template>");
	}

	@Test
	public void testFor() {
		Jerry nodes = parser.parse("<div *ngFor=\"let p of persons; index as i; first as f; last as l; even as e; odd as o\">xx</div>");

		traverse(nodes, visitor);
		assertThat(nodes.get(0)
				.getHtml()).isEqualTo("<ng-template ngFor let-p [ngForOf]=\"persons\" let-i=\"index\" let-f=\"first\" let-l=\"last\" let-e=\"even\" let-o=\"odd\"><div>xx</div></ng-template>");
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
