package ngoy.common;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

/**
 * Outputs the input object pairs array as a {@link java.util.Map}.
 * <p>
 * The input object is inserted at front of the params (key of first pair).
 * 
 * @author krizz
 */
@Pipe("map")
public final class MapPipe implements PipeTransform {

	@Override
	public Map<Object, Object> transform(Object obj, Object... params) {
		List<Object> pairs = ListPipe.transformInternal(obj, params);

		Map<Object, Object> result = new LinkedHashMap<>();
		for (int i = 0, n = pairs.size(); i < n; i += 2) {
			result.put(pairs.get(i), pairs.get(i + 1));
		}
		return result;
	}
}
