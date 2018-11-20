package ngoy;

/**
 * The ngoy version.
 * 
 * @author krizz
 */
public final class Version {

	private static final String IMPLEMENTATION_VERSION;

	static {
		String version = Version.class.getPackage()
				.getImplementationVersion();
		IMPLEMENTATION_VERSION = version == null ? "unknown" : version;
	}

	private Version() {
	}

	/**
	 * @return The ngoy version.
	 */
	public static String getImplementationVersion() {
		return IMPLEMENTATION_VERSION;
	}
}