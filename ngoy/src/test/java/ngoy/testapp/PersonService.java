package ngoy.testapp;

import ngoy.model.Person;

import java.util.List;

import static java.util.Arrays.asList;

public class PersonService {
	public List<Person> getPersons() {
		return asList(new Person("krizz" + System.currentTimeMillis()), new Person("qbert"));
	}
}
