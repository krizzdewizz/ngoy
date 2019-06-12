package ngoy.core.internal;

import ngoy.core.Component;
import ngoy.core.Directive;
import ngoy.core.ModuleWithProviders;
import ngoy.core.NgModule;
import ngoy.core.NgoyException;
import ngoy.core.Pipe;
import ngoy.core.Provide;
import ngoy.core.Provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static ngoy.core.Provider.of;
import static ngoy.core.Provider.useClass;

public class DeclCollector {

    public static class ProviderWithModule {
        public final Provider provider;
        public final String moduleName;

        ProviderWithModule(Provider provider, String moduleName) {
            this.provider = provider;
            this.moduleName = moduleName;
        }
    }

    public static List<Provider> toProviders(List<Class<?>> list) {
        return list.stream()
                .map(Provider::of)
                .collect(toList());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void addModuleDecls(Class<?> mod, Map<String, List<Provider>> targetCmps, Map<String, ProviderWithModule> targetPipes, List<Provider> providers) {

        NgModule ngMod = mod.getAnnotation(NgModule.class);
        if (ngMod != null) {
            addDecls(mod, toProviders(asList(ngMod.declarations())), targetCmps, targetPipes);

            for (Class<?> imp : ngMod.imports()) {
                addModuleDecls(imp, targetCmps, targetPipes, providers);
            }

            for (Class<?> prov : ngMod.providers()) {
                providers.add(of(prov));
            }

            for (Provide prov : ngMod.provide()) {
                Class p = prov.provide();
                Class c = prov.useClass();
                providers.add(useClass(p, c));
            }
        }

        Component cmp = mod.getAnnotation(Component.class);
        if (cmp != null) {

            for (Class<?> prov : cmp.providers()) {
                providers.add(of(prov));
            }

            for (Provide prov : cmp.provide()) {
                Class p = prov.provide();
                Class c = prov.useClass();
                providers.add(useClass(p, (Class<?>) c));
            }
        }
    }

    public static void addModuleDecls(List<ModuleWithProviders<?>> modules, Map<String, List<Provider>> cmpDecls, Map<String, ProviderWithModule> targetPipes, List<Provider> providers, List<Provider> targetProviders) {
        for (ModuleWithProviders<?> mod : modules) {
            addModuleDecls(mod.getModule(), cmpDecls, targetPipes, providers);
            addDecls(mod.getModule(), toProviders(mod.getDeclarations()), cmpDecls, targetPipes);
            targetProviders.addAll(mod.getProviders());
        }
    }

    public static void addDecls(Class<?> mod, List<Provider> providers, Map<String, List<Provider>> targetCmps, Map<String, ProviderWithModule> targetPipes) {
        for (Provider p : providers) {
            Pipe pipe = p.getProvide().getAnnotation(Pipe.class);
            if (pipe != null) {
                String pipeValue = pipe.value();
                ProviderWithModule providerWithModule = targetPipes.get(pipeValue);
                if (providerWithModule != null) {
                    throw new NgoyException("More than one provider found for pipe '%s': %s, %s", pipeValue, providerWithModule.moduleName, mod.getName());
                }
                targetPipes.put(pipeValue, new ProviderWithModule(p, mod.getName()));
            }

            Component cmp = p.getProvide().getAnnotation(Component.class);
            if (cmp != null) {
                putCmp(cmp.selector(), p, targetCmps, false);
            }

            Directive dir = p.getProvide().getAnnotation(Directive.class);
            if (dir != null) {
                putCmp(dir.selector(), p, targetCmps, true);
            }
        }
    }

    static void putCmp(String selector, Provider p, Map<String, List<Provider>> targetCmps, boolean allowMulti) {
        List<Provider> list = targetCmps.get(selector);
        if (list != null && !allowMulti) {
            Provider existing = list.get(0);
            if (existing.getProvide() == p.getProvide()) {
                // double registration on same component
                return;
            }
            throw new NgoyException("More than one component matched on the selector '%s'. Make sure that only one component's selector can match a given element. Conflicting components: %s, %s",
                    selector,
                    existing.getProvide().getName(),
                    p.getProvide().getName());
        }

        list = targetCmps.computeIfAbsent(selector, _k -> new ArrayList<>());
        list.add(p);
    }

    public static Map<Object, Object> getSelectorToCmpDecls(Set<Class<?>> cmpDecls) {
        Map<Object, Object> all = new HashMap<>();
        for (Class<?> cmpDecl : cmpDecls) {
            Component cmp = cmpDecl.getAnnotation(Component.class);
            if (cmp == null) {
                continue;
            }

            String selector = cmp.selector();
            all.put(selector, cmpDecl);
            all.put(cmpDecl, selector);
        }
        return all;
    }

    public static Provider provides(Class<?> theClass, List<Provider> rootProviders) {
        return rootProviders.stream()
                .filter(p -> p.getProvide().equals(theClass))
                .findFirst()
                .orElse(null);
    }
}
