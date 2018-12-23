package ngoy.core.cli;

public class Global {
	public Integer Int(Object s) {
		return s == null ? null : Integer.valueOf(String.valueOf(s));
	}

	public String String(Object s) {
		return s == null ? null : s instanceof String ? (String) s : s.toString();
	}

	public final String nl = System.lineSeparator();
}
