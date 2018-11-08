package org.ngoy.testapp;

import org.ngoy.core.Component;
import org.ngoy.core.Input;
import org.ngoy.model.Person;

@Component(selector = "person-detail", templateUrl = "person-detail.component.html")
public class PersonDetailComponent {
	@Input()
	public Person person;
}
