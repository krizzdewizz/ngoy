package ngoy.testapp;

import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.OnInit;
import ngoy.model.Person;

import java.util.List;

@Component(selector = "person-list", templateUrl = "person-list.component.html")
public class PersonListComponent implements OnInit {
	@Input()
	public List<Person> persons;

	@Override
	public void onInit() {
	}
}
