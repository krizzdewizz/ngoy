package ngoy.model;

import java.util.List;

public class Person {
	private final String name;
	private final int age;
	private List<Person> friends;

	public Person(String name) {
		this(name, 0);
	}

	public Person(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}

	public boolean isTeenager() {
		return age < 20;
	}

	public List<Person> getFriends() {
		return friends;
	}

	public void setFriends(List<Person> friends) {
		this.friends = friends;
	}
}