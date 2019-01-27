package ngoy.common;

import ngoy.core.Pipe;
import ngoy.core.PipeTransform;
import ngoy.core.internal.Ctx;

/**
 * Outputs the input object as raw, unescaped text.
 * 
 * @author krizz
 */
@Pipe("raw")
public final class RawPipe implements PipeTransform {

	@Override
	public Object transform(Object obj, Object... params) {
		// ctx parameter is added in Prefixer
		Ctx ctx = (Ctx) params[0];
		ctx.p(obj);
		return null;
	}
}
