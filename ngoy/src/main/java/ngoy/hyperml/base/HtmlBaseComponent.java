package ngoy.hyperml.base;

import hyperml.base.BaseMl;
import ngoy.core.Inject;
import ngoy.core.Injector;
import ngoy.core.OnCompileStyles;
import ngoy.core.OnRender;
import ngoy.core.Output;

import java.io.StringWriter;

/**
 * Base class for custom code-only components using a subclass of
 * {@link NgoyHtmlBase}.
 *
 * @author krizz
 */
public abstract class HtmlBaseComponent<T extends NgoyHtmlBase<?>> extends NgoyHtmlBase<T> implements OnRender, OnCompileStyles {

    private Runnable renderer;

    @Inject
    public Injector injector;

    @Override
    protected Injector injector() {
        return injector;
    }

    @Override
    public void onRender(Output output) {
        try {
            renderer = this::content;
            build(output);
        } finally {
            renderer = null;
        }
    }

    @Override
    public String onCompileStyles() {
        StringWriter sw = new StringWriter();
        BaseMl<?> doc = stylesDocument();
        if (doc != null) {
            doc.build(sw);
        } else {
            try {
                renderer = this::styles;
                build(sw);
            } finally {
                renderer = null;
            }
        }
        return sw.toString();
    }

    protected BaseMl<?> stylesDocument() {
        return null;
    }

    protected void styles() {
    }

    abstract protected void content();

    @Override
    protected void create() {
        renderer.run();
    }
}
