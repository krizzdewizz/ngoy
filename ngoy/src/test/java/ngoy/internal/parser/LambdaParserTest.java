package ngoy.internal.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ngoy.internal.parser.LambdaParser.Lambda;

public class LambdaParserTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void noLambda() {
		assertThat(parse("")).isNull();
		assertThat(parse("a")).isNull();
		assertThat(parse(null)).isNull();
		assertThat(LambdaParser.parse("int x = 0;")).isEqualTo("int x = 0;");
	}

	@Test
	public void noParams() {
		Lambda md = parse("Runnable q = ()->foo(x).noop(a ? x : p)");
		assertThat(md.params.size()).isEqualTo(0);
		assertThat(md.expression).isEqualTo("foo(x).noop(a ? x : p)");
	}

	@Test
	public void rest() {
		String parsed = LambdaParser.parse("java.util.stream.Stream.of('a', 'b', 'c').map(c -> c + 'x').qbert", 0, null, null);
		assertThat(parsed).isEqualTo("java.util.stream.Stream.of('a', 'b', 'c').map(new LAMBDA(){public Object LAMBDA_METH(Object c){return  c + 'x';}}).qbert");
//		assertThat(md.expression).isEqualTo("foo(x).noop(a ? x : p)");
	}

	@Test
	public void asParameter() {
		Lambda md = parse("filter(() -> foo(x).noop(a ? x : p) + 'x')");
		assertThat(md.params.size()).isEqualTo(0);
		assertThat(md.expression).isEqualTo(" foo(x).noop(a ? x : p) + 'x'");
	}

	@Test
	public void singleParam() {
		Lambda md = parse("(x) -> x");
		assertThat(md.params.size()).isEqualTo(1);
		assertThat(md.params.get(0).type).isEqualTo("");
		assertThat(md.params.get(0).name).isEqualTo("x");
		assertThat(md.expression).isEqualTo(" x");
	}

	@Test
	public void singleParamNoParens() {
		Lambda md = parse("x -> x");
		assertThat(md.params.size()).isEqualTo(1);
		assertThat(md.params.get(0).type).isEqualTo("");
		assertThat(md.params.get(0).name).isEqualTo("x");
		assertThat(md.expression).isEqualTo(" x");
	}

	@Test
	public void singleParamWithType() {
		Lambda l = parse("(int x) -> x");
		assertThat(l.params.size()).isEqualTo(1);
		assertThat(l.params.get(0).type).isEqualTo("int");
		assertThat(l.params.get(0).name).isEqualTo("x");
		assertThat(l.expression).isEqualTo(" x");
	}

	private Lambda parse(String s) {
		Lambda[] q = new Lambda[1];
		LambdaParser.parse(s, 0, q, null);
		return q[0];
	}

	@Test
	public void twoParams() {
		Lambda md = parse("(x, y) -> x");
		assertThat(md.params.size()).isEqualTo(2);
		assertThat(md.params.get(0).type).isEqualTo("");
		assertThat(md.params.get(0).name).isEqualTo("x");
		assertThat(md.params.get(1).type).isEqualTo("");
		assertThat(md.params.get(1).name).isEqualTo("y");
		assertThat(md.expression).isEqualTo(" x");
	}

	@Test
	public void twoParamsWithType() {
		Lambda md = parse("(int x, String y) -> x");
		assertThat(md.params.size()).isEqualTo(2);
		assertThat(md.params.get(0).type).isEqualTo("int");
		assertThat(md.params.get(0).name).isEqualTo("x");
		assertThat(md.params.get(1).type).isEqualTo("String");
		assertThat(md.params.get(1).name).isEqualTo("y");
		assertThat(md.expression).isEqualTo(" x");
	}

	@Test
	public void toAnon() {
		String anon = LambdaParser.parse("x -> x");
		assertThat(anon).isEqualTo("new LAMBDA(){public Object LAMBDA_METH(Object x){return  x;}}");
	}

	@Test
	public void withType() {
		String anon = LambdaParser.parse("(int x) -> x");
		assertThat(anon).isEqualTo("new LAMBDA(){public Object LAMBDA_METH(int x){return  x;}}");
	}

	@Test
	public void withTwoParams() {
		String anon = LambdaParser.parse("(x, int y) -> x");
		assertThat(anon).isEqualTo("new LAMBDA(){public Object LAMBDA_METH(Object x,int y){return  x;}}");
	}

	@Test
	public void param() {
		String anon = LambdaParser.parse("foo((x, int y) -> x)");
		assertThat(anon).isEqualTo("foo(new LAMBDA(){public Object LAMBDA_METH(Object x,int y){return  x;}})");
	}

	@Test
	public void multi() {
		String anon = LambdaParser.parse("filter(x -> x).map(a -> a)");
		assertThat(anon).isEqualTo("filter(new LAMBDA(){public Object LAMBDA_METH(Object x){return  x;}}).map(new LAMBDA(){public Object LAMBDA_METH(Object a){return  a;}})");
	}

	@Test
	public void inString() {
		String anon = LambdaParser.parse("\"(a ->, x\"");
		assertThat(anon).isEqualTo("\"(a ->, x\"");
	}

	@Test
	public void inString2() {
		String anon = LambdaParser.parse("\"(a ->, \\\" x\"");
		assertThat(anon).isEqualTo("\"(a ->, \\\" x\"");
	}

	@Test
	public void inString3() {
		String anon = LambdaParser.parse("foo((x, int y) -> \" -> \").bar(s -> s)");
		assertThat(anon).isEqualTo("foo(new LAMBDA(){public Object LAMBDA_METH(Object x,int y){return  \" -> \";}}).bar(new LAMBDA(){public Object LAMBDA_METH(Object s){return  s;}})");
	}
}
