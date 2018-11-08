package org.ngoy.testapp;

import static java.util.Arrays.asList;

import java.util.List;

import org.ngoy.model.Person;

public class PersonService {
	public List<Person> getPersons() {
		return asList(new Person("krizz" + System.currentTimeMillis()), new Person("qbert"));
	}
}
