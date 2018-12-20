package ngoy.common.cmp;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

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

	@Component(selector = "test", template = "<ng-container *ngFor=\"let entry : java.util.stream.Stream.of('a', 'b', 'c')\">{{entry.trim()}}</ng-container>")
	public static class CmpStream2 {
	}

	@Test
	public void testStream2() {
		assertThat(render(CmpStream2.class)).isEqualTo("abc");
	}

}
