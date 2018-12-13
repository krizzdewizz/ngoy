package ngoy.parser;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.core.internal.Ctx;
import ngoy.model.Person;

public class JavaParserTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Component(selector = "person", template = "hello: {{ person.getName() | date:\"YYYY\" }}")
	public static class PersonCmp {
		@Input
		public Person person;

		@Input
		public void setPerson2(@SuppressWarnings("unused") Person person) {
			//
		}
	}

	@Component(selector = "", template = "<pre class='x' [class.a]='1==1' [style.color]='appName' [style.width.px]='10' style='white-space:nowrap;qbert:red'  ></pre>app: {{ appName }}" //
			+ "<b *ngFor='let hobby of getHobbies(); index as i; first as fir'>{{hobby}}{{i}}{{fir}}</b>" //
			+ "<person *ngIf='appName.equals(\"a\")' [person]=\"peter\", [person2]=\"peter\"></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Cmp {
		public String appName = "theApp";
		public Person peter = new Person("Peter");

//		public List<String> hobbies = asList("a", "b", "c");
		public List<String> getHobbies() {
			return asList("a", "b", "c");
		}
//		public String[] hobbies = new String[] { "a", "b", "c" };
	}

	public Map<String, String> getters(Class<?> clazz) {
		return Stream.of(clazz.getMethods())
				.filter(m -> m.getReturnType() != void.class && m.getName()
						.startsWith("get") && m.getParameterCount() == 0)
				.collect(toMap(this::toFieldName, Method::getName));
	}

	String toFieldName(Method meth) {
		String name = meth.getName();
		String field = name.substring("get".length());
		return field.substring(0, 1)
				.toLowerCase() + field.substring(1);
	}

	// maybe used sometime
	String convertFieldAccess(Map<String, String> getters, String identifier) {
		String getter = getters.get(identifier);
		if (getter != null) {
			return format("%s()", getter);
		}

		return identifier;
	}

	public static class MyTemplate {
		static String code;

		public static void render(Ctx ctx) {
			ctx.print(code);
		}
	}
}
