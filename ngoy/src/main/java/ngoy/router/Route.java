package ngoy.router;

/**
 * A route consist of a path and a component that will be rendered when the
 * route/path is the current one.
 * 
 * @author krizz
 */
public class Route {
	public static Route of(String path, Class<?> component) {
		return new Route(path, component);
	}

	private final String path;
	private final Class<?> component;

	private Route(String path, Class<?> component) {
		this.path = path;
		this.component = component;
	}

	public String getPath() {
		return path;
	}

	public Class<?> getComponent() {
		return component;
	}

}
