package org.ngoy.internal.parser.visitor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jsoup.nodes.Node;
import org.junit.Before;
import org.junit.Test;
import org.ngoy.internal.parser.Parser;

public class SwitchToElseIfVisitorTest {

	private SwitchToElseIfVisitor visitor;

	@Before
	public void beforeEach() {
		visitor = new SwitchToElseIfVisitor(new DefaultNodeVisitor());
	}

	@Test
	public void test() {

		Parser parser = new Parser();

		String template = "" //
				+ "<div [ngSwitch]=\"emotion\">\n" + //
				"    <ng-template [ngSwitchCase]=\"'happy'\"><div>HAPPY</div></ng-template>\n" + //
				"    <ng-template [ngSwitchCase]=\"'sad'\"><div>SAD</div></ng-template>\n" + //
				"    <ng-template ngSwitchDefault><div>NONE</div></ng-template>" + //
				"</div>";
		List<Node> nodes = parser.parse(template, false);

		nodes.forEach(n -> n.traverse(visitor));
		assertThat(nodes.get(0)
				.toString()).isEqualTo("<div>\n" + //
						" <ng-template ngIfForSwitch [ngIf]=\"emotion\" ngElseIfFirst-case0=\"'happy'\" ngElseIf-case1=\"'sad'\" ngElse=\"case2\"> \n" + //
						"  <ng-template #case0>\n" + //
						"   <div>\n" + //
						"    HAPPY\n" + //
						"   </div>\n" + //
						"  </ng-template> \n" + //
						"  <ng-template #case1>\n" + //
						"   <div>\n" + //
						"    SAD\n" + //
						"   </div>\n" + //
						"  </ng-template> \n" + //
						"  <ng-template #case2>\n" + //
						"   <div>\n" + //
						"    NONE\n" + //
						"   </div>\n" + //
						"  </ng-template>\n" + //
						" </ng-template>\n" + //
						"</div>");
	}
}
