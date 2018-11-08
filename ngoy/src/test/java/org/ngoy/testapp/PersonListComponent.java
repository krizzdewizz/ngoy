package org.ngoy.testapp;

import java.util.List;

import org.ngoy.core.Component;
import org.ngoy.core.Input;
import org.ngoy.core.OnInit;
import org.ngoy.model.Person;

@Component(selector = "person-list", templateUrl = "person-list.component.html")
public class PersonListComponent implements OnInit {
	@Input()
	public List<Person> persons;

	@Override
	public void ngOnInit() {
	}
}
