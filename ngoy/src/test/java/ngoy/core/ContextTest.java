package ngoy.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.model.Person;

public class ContextTest {

	@Test
	public void testOf() {
		assertThat(Context.of()).isNotNull();
	}

	@Test
	public void testOfRoot() {
		assertThat(Context.of(Object.class, new Object())).isNotNull();
	}

	@Test
	public void testOfRoot2() {
		Context<Person> context = Context.of(new Person("Peter", 22));
		assertThat(context).isNotNull();
		assertThat(context.getModelClass()).isEqualTo(Person.class);
	}

	@Test
	public void testVariable() {
		Context<?> of = Context.of();
		Context<?> context = of.variable("a", String.class, "1");
		assertThat(context).isSameAs(of);
	}

	@Test
	public void testVariableDeduceClass() {
		Context<?> context = Context.of()
				.variable("a", "1");
		assertThat(context.getVariables()
				.get("a").type).isEqualTo(String.class);
	}

	@Test
	public void testVariableDeduceClass2() {
		Context<?> context = Context.of("a", "1");
		assertThat(context.getVariables()
				.get("a").type).isEqualTo(String.class);
	}
}
