package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ForOf2Test extends ANgoyTest {

	@Component(selector = "test", template = "<ng-container *ngFor='let it of new int[] {1, 2, 3}'>{{it}}</ng-container>")
	public static class CmpForOf {
	}

	@Test
	public void testForOf() {
		assertThat(render(CmpForOf.class)).isEqualTo("123");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"let it of new ngoy.model.Person[] {new ngoy.model.Person('Peter', 22), new ngoy.model.Person('Mary', 23)}\">{{it.name}}</ng-container>")
	public static class CmpForOfPerson {
	}

	@Test
	public void testForOfPerson() {
		assertThat(render(CmpForOfPerson.class)).isEqualTo("PeterMary");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"ngoy.model.Person it of java.util.Arrays.asList(new ngoy.model.Person('Peter', 22), new ngoy.model.Person('Mary', 23))\">{{it.name}}</ng-container>")
	public static class CmpForOfPersonList {
	}

	@Test
	public void testForOfPersonList() {
		assertThat(render(CmpForOfPersonList.class)).isEqualTo("PeterMary");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"let it of java.util.Arrays.asList(new ngoy.model.Person('Peter', 22), new ngoy.model.Person('Mary', 23))\">{{it.name}}</ng-container>")
	public static class CmpForOfPersonListCast {
	}

	@Test
	public void testForOfPersonListCast() {
		assertThat(render(CmpForOfPersonListCast.class)).isEqualTo("PeterMary");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"let entry of entries\">{{entry.get(0)}}</ng-container>")
	public static class CmpEntries {
		public List<List<Object>> entries = asList(asList("hello"));

		public List<List<Object>> getEntries2() {
			return asList(asList("hello"));
		}
	}

	@Test
	public void testEntries() {
		assertThat(render(CmpEntries.class)).isEqualTo("hello");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"let entry of entries\">{{entry}}</ng-container>")
	public static class CmpStream {
		public Stream<String> getEntries() {
			return Stream.of("a", "b", "c");
		}
	}

	@Test
	public void testStream() {
		assertThat(render(CmpStream.class)).isEqualTo("abc");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"let entry : java.util.stream.Stream.of('a ', ' b', ' c ').map(c -> c.trim() + 'x')\">{{entry}}</ng-container>")
	public static class CmpStream2 {
	}

	@Test
	public void testStream2() {
		assertThat(render(CmpStream2.class)).isEqualTo("axbxcx");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"let entry : java.util.stream.Stream.of('a ', ' b', ' c ').filter(it -> !it.contains('a')).map(c -> c.trim() + 'x')\">{{entry.trim()}}</ng-container>")
	public static class CmpStream3 {
	}

	@Test
	public void testStream3() {
		assertThat(render(CmpStream3.class)).isEqualTo("bxcx");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"let entry : java.util.stream.Stream.of(qbert, ' b', ' cc ').map(c -> c.trim())\">{{entry}}</ng-container>")
	public static class CmpStream4 {
		public String qbert = "qbert";
	}

	@Test
	public void testStream4() {
		assertThat(render(CmpStream4.class)).isEqualTo("qbertbcc");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"let entry : java.util.stream.Stream.of('aa ', ' b', ' cc ').map(c -> c.trim().length() > 1)\">{{entry}}</ng-container>")
	public static class CmpStream5 {
	}

	@Test
	public void testStream5() {
		assertThat(render(CmpStream5.class)).isEqualTo("truefalsetrue");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"let entry : java.util.stream.Stream.of(' b', ' cc ').map(c -> qbert + c.trim())\">{{entry}}</ng-container>")
	public static class CmpStream6 {
		public String qbert = "qbert";
	}

	@Test
	public void testStream6() {
		assertThat(render(CmpStream6.class)).isEqualTo("qbertbqbertcc");
	}

	//

	@Component(selector = "test", template = "<li *ngFor='let x of java.util.stream.Stream.of(1, 2, 3).filter(x -> x > 1)'>{{x}}</li>")
	public static class CmpStream7 {
	}

	@Test
	public void testStream7() {
		assertThat(render(CmpStream7.class)).isEqualTo("<li>2</li><li>3</li>");
	}
}
