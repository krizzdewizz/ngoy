package ngoy.common;

import java.util.ArrayList;
import java.util.List;

import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

/**
 * Outputs the input object array as a {@link java.util.List}.
 * 
 * @author krizz
 */
@Pipe("list")
public final class ListPipe implements PipeTransform {

	static List<Object> transformInternal(Object obj, Object... params) {
		List<Object> result = new ArrayList<>();

		result.add(obj);
		for (int i = 0, n = params.length; i < n; i++) {
			result.add(params[i]);
		}

		return result;
	}

	@Override
	public List<Object> transform(Object obj, Object... params) {
		return transformInternal(obj, params);
	}

}
