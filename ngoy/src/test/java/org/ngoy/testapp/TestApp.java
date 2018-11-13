package org.ngoy.testapp;

import static java.util.Collections.emptyList;

import java.util.List;

import org.ngoy.common.TranslateModule;
import org.ngoy.core.Component;
import org.ngoy.core.Inject;
import org.ngoy.core.NgModule;
import org.ngoy.core.OnInit;
import org.ngoy.model.Person;

@Component(selector = "person-list", templateUrl = "test-app.html")
@NgModule(imports = { TranslateModule.class }, declarations = { PersonDetailComponent.class, PersonListComponent.class })
public class TestApp implements OnInit {

	@Inject
	public PersonService personService;

	public List<Person> persons = emptyList();
	public boolean show = true;

	@Override
	public void ngOnInit() {
		persons = personService.getPersons();
	}
}
