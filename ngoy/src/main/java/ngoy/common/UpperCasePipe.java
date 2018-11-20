package ngoy.common;

import ngoy.core.Inject;
import ngoy.core.LocaleProvider;
import ngoy.core.Nullable;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

/**
 * Transforms the input to uppercase using the current locale.
 * <p>
 * Example:
 * <p>
 * 
 * <pre>
 *  
 * {{ 'hello' | lowercase }}
 * </pre>
 * <p>
 * Output:
 * 
 * <pre>
 * HELLO
 * </pre>
 * 
 * @author krizz
 */
@Pipe("uppercase")
public class UpperCasePipe implements PipeTransform {

	@Inject
	public LocaleProvider locale;

	@Override
	public Object transform(@Nullable Object obj, Object... params) {
		if (obj == null) {
			return null;
		}

		return obj.toString()
				.toUpperCase(locale.get());
	}

}
