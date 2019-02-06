package ngoy.core.internal;

import ngoy.core.RenderException;

public interface TemplateRender {
	void render(Ctx ctx) throws RenderException;
}
