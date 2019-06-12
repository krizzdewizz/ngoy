package ngoy.internal.scan;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import ngoy.core.Component;
import ngoy.core.Directive;
import ngoy.core.Injectable;
import ngoy.core.ModuleWithProviders;
import ngoy.core.Pipe;
import ngoy.core.Provider;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class ClassScanner {

    private final Set<String> excludeClassNames = new HashSet<>();

    public ClassScanner excludeClassNames(String... classNames) {
        excludeClassNames.addAll(asList(classNames));
        return this;
    }

    public ModuleWithProviders<?> scan(String... packagePrefixes) {

        ClassGraph classGraph = new ClassGraph().blacklistClasses(excludeClassNames.toArray(new String[0]))
                .whitelistPackages(packagePrefixes)
                .enableAnnotationInfo();

        try (ScanResult scanResult = classGraph.scan()) {
            Class<?>[] declarations = Stream.of(Component.class, Directive.class, Pipe.class)
                    .map(Class::getName)
                    .flatMap(c -> scanResult.getClassesWithAnnotation(c).stream())
                    .map(ClassInfo::loadClass)
                    .toArray(Class[]::new);

            Provider[] providers = scanResult.getClassesWithAnnotation(Injectable.class.getName())
                    .stream()
                    .map(ClassInfo::loadClass)
                    .map(Provider::of)
                    .toArray(Provider[]::new);

            return ModuleWithProviders.of(Void.class)
                    .declarations(declarations)
                    .providers(providers)
                    .build();
        }
    }
}
