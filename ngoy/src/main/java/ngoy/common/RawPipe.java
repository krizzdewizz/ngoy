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
		// ctx parameter is inserted at front unlike all the other pipes. see ExprParser
		Ctx ctx = (Ctx) obj;
		ctx.p(params[0]);
		return null;
	}
}
