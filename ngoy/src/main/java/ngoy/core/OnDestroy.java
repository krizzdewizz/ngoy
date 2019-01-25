package ngoy.core;

/**
 * Descruction lifecycle hook.
 * <p>
 * A component or directive may implement this interface to cleanup after
 * itself.
 * <p>
 * {@link #onDestroy()} is called in the rendering phase when the component
 * gets out of scope.
 * 
 * @author krizz
 */
public interface OnDestroy {
	void onDestroy();
}
