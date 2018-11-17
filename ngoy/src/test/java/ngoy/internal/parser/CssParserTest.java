package ngoy.internal.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.helger.css.ECSSVersion;
import com.helger.css.decl.CSSDeclarationList;
import com.helger.css.reader.CSSReaderDeclarationList;

public class CssParserTest {
	@Test
	public void test() throws Exception {
		final String sStyle = "color:red; background:fixed !important";
		final CSSDeclarationList aDeclList = CSSReaderDeclarationList.readFromString(sStyle, ECSSVersion.CSS30);

		assertThat(aDeclList.size()).isEqualTo(2);
		assertThat(aDeclList.get(0).getProperty()).isEqualTo("color");
		assertThat(aDeclList.get(0).getExpressionAsCSSString()).isEqualTo("red");
		assertThat(aDeclList.get(1).getProperty()).isEqualTo("background");
		assertThat(aDeclList.get(1).getExpressionAsCSSString()).isEqualTo("fixed"); // !important ??
	}
}
