package ngoy.core;

/**
 * Initialization lifecycle hook.
 * <p>
 * A component or directive may implement this interface to initialize itself.
 * <p>
 * {@link #ngOnInit()} is called just before the component is rendered.
 * 
 * @author krizz
 */
public interface OnInit {
	void ngOnInit();
}
