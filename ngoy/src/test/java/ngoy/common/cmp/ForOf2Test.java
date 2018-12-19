package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.common.cmp.CmpTest.PersonCmp;
import ngoy.core.Component;
import ngoy.core.NgModule;

public class ForOf2Test extends ANgoyTest {

	@Component(selector = "test", template = "<ng-container *ngFor='let it of new int[] {1, 2, 3}'>{{it}}</ng-container>")
	@NgModule(declarations = { PersonCmp.class })
	public static class CmpForOf {
	}

	@Test
	public void testForOf() {
		assertThat(render(CmpForOf.class)).isEqualTo("123");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"let it of new ngoy.model.Person[] {new ngoy.model.Person('Peter', 22), new ngoy.model.Person('Mary', 23)}\">{{it.name}}</ng-container>")
	@NgModule(declarations = { PersonCmp.class })
	public static class CmpForOfPerson {
	}

	@Test
	public void testForOfPerson() {
		assertThat(render(CmpForOfPerson.class)).isEqualTo("PeterMary");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"let it of java.util.Arrays.asList(new ngoy.model.Person('Peter', 22), new ngoy.model.Person('Mary', 23))\">{{((ngoy.model.Person)it).name}}</ng-container>")
	@NgModule(declarations = { PersonCmp.class })
	public static class CmpForOfPersonListCast {
	}

	@Test
	public void testForOfPersonListCast() {
		assertThat(render(CmpForOfPersonListCast.class)).isEqualTo("PeterMary");
	}

	//

	@Component(selector = "test", template = "<ng-container *ngFor=\"ngoy.model.Person it of java.util.Arrays.asList(new ngoy.model.Person('Peter', 22), new ngoy.model.Person('Mary', 23))\">{{it.name}}</ng-container>")
	@NgModule(declarations = { PersonCmp.class })
	public static class CmpForOfPersonList {
	}

	@Test
	public void testForOfPersonList() {
		assertThat(render(CmpForOfPersonList.class)).isEqualTo("PeterMary");
	}
}
