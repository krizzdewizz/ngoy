package ngoy.router;

/**
 * The current location/path used to determine the active route.
 * 
 * @author krizz
 */
public interface Location {
	/**
	 * Returns the current path such as <code>/app/settings</code>.
	 * 
	 * @return Path
	 */
	String getPath();
}
