package ngoy;

public final class Version {

	private static final String IMPLEMENTATION_VERSION;

	static {
		String version = Version.class.getPackage()
				.getImplementationVersion();
		IMPLEMENTATION_VERSION = version == null ? "unknown" : version;
	}

	private Version() {
	}

	public static String getImplementationVersion() {
		return IMPLEMENTATION_VERSION;
	}
}