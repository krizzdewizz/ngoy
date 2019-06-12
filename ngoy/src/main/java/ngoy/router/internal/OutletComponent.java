package ngoy.router.internal;

import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.Injector;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;
import ngoy.core.OnRender;
import ngoy.core.Output;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.Scope;
import ngoy.core.internal.TemplateCompiler;
import ngoy.core.internal.TemplateRender;
import ngoy.core.internal.TemplateRenderCache;
import ngoy.router.Route;
import ngoy.router.Router;

@Component(selector = "router-outlet", template = "<ng-content></ng-content>")
@Scope
public class OutletComponent implements OnDestroy, OnRender {
    @Inject
    public Router router;

    @Inject
    public TemplateCompiler compiler;

    @Inject
    public Injector injector;

    @Override
    public void onRender(Output output) {
        Route route = router.getRoutes().get(router.getActiveRoute());

        Class<?> routeClass = route.getComponent();

        OnRender render;
        Object cmp = injector.getNew(routeClass);
        if (cmp instanceof OnRender) {
            render = (OnRender) cmp;
        } else {
            TemplateRender templateRender = TemplateRenderCache.INSTANCE.compile(routeClass, compiler);
            render = out -> templateRender.render(new Ctx(cmp, injector, out.getWriter()));
        }

        if (cmp instanceof OnInit) {
            ((OnInit) cmp).onInit();
        }

        String selector = getSelector(routeClass);

        output.write("<");
        output.write(selector);
        output.write(">");

        render.onRender(output);
        render.onRenderEnd(output);

        output.write("</");
        output.write(selector);
        output.write(">");

        if (cmp instanceof OnDestroy) {
            ((OnDestroy) cmp).onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        router.clearRouteParams();
    }

    private String getSelector(Class<?> component) {
        return component.getAnnotation(Component.class).selector();
    }

}
