package ngoy.common;

import java.util.LinkedHashSet;
import java.util.Set;

import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

/**
 * Outputs the input object array as a {@link java.util.Set}.
 * 
 * @author krizz
 */
@Pipe("set")
public final class SetPipe implements PipeTransform {

	@Override
	public Set<Object> transform(Object obj, Object... params) {
		Set<Object> result = new LinkedHashSet<>();

		result.add(obj);
		for (int i = 0, n = params.length; i < n; i++) {
			result.add(params[i]);
		}

		return result;
	}
}
