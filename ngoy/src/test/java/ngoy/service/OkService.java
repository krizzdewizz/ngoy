package ngoy.service;

public class OkService {
	public static final OkService OK = new OkService(true);
	public static final OkService NOK = new OkService(false);

	public final boolean ok;

	private OkService(boolean ok) {
		this.ok = ok;
	}
}