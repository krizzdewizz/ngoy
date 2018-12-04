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
	 * @return The ngoy version with timestamp.
	 */
	public static String getImplementationVersion() {
		return IMPLEMENTATION_VERSION;
	}

	/**
	 * @return The ngoy version without timestamp.
	 */
	public static String getVersion() {
		int pos = IMPLEMENTATION_VERSION.indexOf('(');
		return pos < 0 ? IMPLEMENTATION_VERSION
				: IMPLEMENTATION_VERSION.substring(0, pos)
						.trim();
	}
}