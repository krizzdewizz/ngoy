package ngoy.router;

import java.util.HashMap;
import java.util.Map;

public class RouteParams {

	private final Map<String, String> params = new HashMap<>();

	public String get(String name) {
		return params.get(name);
	}

	public RouteParams put(String name, String value) {
		params.put(name, value);
		return this;
	}
}
