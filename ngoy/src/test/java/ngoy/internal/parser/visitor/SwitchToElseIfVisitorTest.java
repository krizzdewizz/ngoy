package ngoy.internal.parser.visitor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import jodd.jerry.Jerry;
import ngoy.core.dom.NodeVisitor;
import ngoy.core.dom.XDom;
import ngoy.internal.parser.Parser;

public class SwitchToElseIfVisitorTest {

	private SwitchToElseIfVisitor visitor;

	@Before
	public void beforeEach() {
		visitor = new SwitchToElseIfVisitor(new NodeVisitor.Default());
	}

	@Test
	public void test() {

		String template = "" //
				+ "<div [ngSwitch]=\"emotion\">\n" + //
				"    <ng-template [ngSwitchCase]=\"'happy'\"><div>HAPPY</div></ng-template>\n" + //
				"    <ng-template [ngSwitchCase]=\"'sad'\"><div>SAD</div></ng-template>\n" + //
				"    <ng-template ngSwitchDefault><div>NONE</div></ng-template>" + //
				"</div>";
		Parser parser = new Parser();
		Jerry nodes = parser.parse(template);

		nodes.forEach(n -> XDom.accept(n, visitor));
		assertThat(XDom.getHtml(nodes)).isEqualTo("<div><ng-template ngIfForSwitch [ngIf]=\"emotion\" ngElseIfFirst-case0=\"'happy'\" ngElseIf-case1=\"'sad'\" ngElse=\"case2\"><div>\n" + //
				"    <ng-template #case0><div>HAPPY</div></ng-template>\n" + //
				"    <ng-template #case1><div>SAD</div></ng-template>\n" + //
				"    <ng-template #case2><div>NONE</div></ng-template></div></ng-template></div>");
	}
}
