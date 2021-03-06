package ngoy.core;

import jodd.jerry.Jerry;

/**
 * Compile lifecycle hook.
 * <p>
 * When the template is parsed/compiled, a component or directive implementing
 * this interface has the opportunity to alter the template's
 * <code>subtree</code> before the parser/compiler sees it.
 * 
 * @author krizz
 */
public interface OnCompile {
	/**
	 * @param el
	 *            The element of the component/directive. Change this element
	 *            and it's subtree to alter/extend the template
	 * @param componentClass
	 *            Component class
	 */
	void onCompile(Jerry el, String componentClass);
}
