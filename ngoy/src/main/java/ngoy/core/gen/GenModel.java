package ngoy.core.gen;

import static java.lang.String.format;
import static ngoy.core.Util.isSet;

public class GenModel {

    private static String camelToKebap(String className) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = className.length(); i < n; i++) {
            char c = className.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('-');
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    public final String pack;
    public final String name;
    public final String className;
    public final String appPrefix;
    public final String ngoyVersion;
    public final String prefixedName;

    public GenModel(String appPrefix, String fqName, String ngoyVersion) {
        this.appPrefix = appPrefix;

        int dot = fqName.lastIndexOf('.');

        this.pack = dot < 0 ? "" : fqName.substring(0, dot);
        className = dot < 0 ? fqName : fqName.substring(dot + 1);
        name = camelToKebap(className);
        this.ngoyVersion = ngoyVersion;

        prefixedName = isSet(appPrefix) ? format("%s-%s", appPrefix, name) : name;
    }
}
