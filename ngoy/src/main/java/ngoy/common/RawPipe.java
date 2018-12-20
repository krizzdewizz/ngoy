package ngoy.common;

import ngoy.core.Pipe;
import ngoy.core.PipeTransform;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.WithCtx;

@Pipe("raw")
public class RawPipe implements PipeTransform, WithCtx {
	private Ctx ctx;

	@Override
	public Object transform(Object obj, Object... params) {
		if (obj != null) {
			ctx.p(obj);
		}

		return null;
	}

	@Override
	public void setCtx(Ctx ctx) {
		this.ctx = ctx;
	}
}
