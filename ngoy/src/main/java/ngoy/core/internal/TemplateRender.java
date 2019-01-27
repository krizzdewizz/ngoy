package ngoy.core.internal;

public interface TemplateRender {
	void render(Ctx ctx, Object cmpInstance) throws RenderException;
}
