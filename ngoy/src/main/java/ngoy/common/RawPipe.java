package ngoy.common;

import ngoy.core.Pipe;
import ngoy.core.PipeTransform;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.WithCtx;

/**
 * Outputs the input object as raw, unescaped text.
 * 
 * @author krizz
 */
@Pipe("raw")
public class RawPipe implements PipeTransform, WithCtx {
	private Ctx ctx;

	@Override
	public Object transform(Object obj, Object... params) {
		ctx.p(obj);
		return null;
	}

	@Override
	public void setCtx(Ctx ctx) {
		this.ctx = ctx;
	}
}
