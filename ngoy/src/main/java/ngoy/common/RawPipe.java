package ngoy.common;

import ngoy.core.Output;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

/**
 * Outputs the input object as raw, unescaped text.
 * 
 * @author krizz
 */
@Pipe("raw")
public final class RawPipe implements PipeTransform {

	@Override
	public Object transform(Object obj, Object... params) {
		if (obj == null) {
			return null;
		}
		// ctx parameter is added in Prefixer
		Output out = (Output) params[0];
		out.write(obj.toString());
		return null;
	}
}
