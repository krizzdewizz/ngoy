package ngoy.core.gen;

import ngoy.common.CapitalizePipe;

public class GenModel {

	private static String camelCaseToJavaClass(String className) {
		String camelCase = new CapitalizePipe().transform(className)
				.toString();
		StringBuilder sb = new StringBuilder();

		boolean nextUpper = false;
		for (int i = 0, n = camelCase.length(); i < n; i++) {
			char c = camelCase.charAt(i);
			if (c == '-') {
				nextUpper = true;
				continue;
			}
			if (nextUpper || i == 0) {
				c = Character.toUpperCase(c);
				nextUpper = false;
			}
			sb.append(c);
		}

		return sb.toString();
	}

	private final String pack;
	private final String name;
	private final String className;

	public GenModel(String pack, String name) {
		this.pack = pack;
		this.name = name;
		className = camelCaseToJavaClass(name);
	}

	public String getPack() {
		return pack;
	}

	public String getName() {
		return name;
	}

	public String getClassName() {
		return className;
	}
}
