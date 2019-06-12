package ngoy.testapp;

import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.core.OnInit;
import ngoy.model.Person;
import ngoy.translate.TranslateModule;

import java.util.List;

import static java.util.Collections.emptyList;

@Component(selector = "person-list", templateUrl = "test-app.html")
@NgModule(imports = { TranslateModule.class }, declarations = { PersonDetailComponent.class, PersonListComponent.class })
public class TestApp implements OnInit {

	@Inject
	public PersonService personService;

	public List<Person> persons = emptyList();
	public boolean show = true;

	@Override
	public void onInit() {
		persons = personService.getPersons();
	}
}
