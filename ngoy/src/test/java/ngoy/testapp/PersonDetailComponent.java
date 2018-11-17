package ngoy.testapp;

import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.model.Person;

@Component(selector = "person-detail", templateUrl = "person-detail.component.html")
public class PersonDetailComponent {
	@Input()
	public Person person;
}
