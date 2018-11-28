package ngoy.core.dom;

import static ngoy.core.dom.NgoyElement.getPosition;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import jodd.jerry.Jerry;

public class NgoyElementTest {

	private static final String HTML = "" //
			+ "<!DOCTYPE html>\n" + //
			"<html lang=\"en\">\n" + //
			"\n" + //
			"<body>\n" + //
			"	<div class=\"container\">\n" + //
			"		<h1>{{appName}}</h1>\n" + //
			"\n" + //
			"		<ul class=\"nav nav-tabs\">\n" + //
			"			<li class=\"nav-item\" *ngFor=\"let route of routes\">\n" + //
			"				<a class=\"nav-link\" [class.active]=\"isActiveRoute(route)\" [routerLink]=\"route.path | localeParam\">{{ route  | routeTitle }}</a>\n" + //
			"			</li>\n" + //
			"		</ul>\n" + //
			"\n" + //
			"		<router-outlet></router-outlet>\n" + //
			"	</div>\n" + //
			"</body>\n" + //
			"\n" + //
			"</html>";

	@Test
	public void lineFix() {
		Jerry html = XDom.parseHtml(HTML, 0);

		Jerry ul = html.$("ul");
		assertThat(getLine(ul)).isEqualTo(8);
		assertThat(getLine(ul.$("a"))).isEqualTo(10);

		Jerry ulClone = XDom.cloneNode(ul);
		assertThat(getLine(ulClone)).isEqualTo(9);
		assertThat(getLine(ulClone.$("a"))).isEqualTo(11);
	}

	private int getLine(Jerry el) {
		return getPosition(el).getLine();
	}
}
