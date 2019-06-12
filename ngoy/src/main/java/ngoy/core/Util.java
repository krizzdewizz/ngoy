package ngoy.core;

import org.codehaus.commons.compiler.CompileException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static ngoy.core.NgoyException.wrap;

/**
 * Utils.
 *
 * @author krizz
 */
public class Util {

    private Util() {
    }

    /**
     * Copies all bytes from in to out.
     *
     * @param in
     * @param out
     */
    public static void copy(InputStream in, OutputStream out) {
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = in.read(buffer)) > -1) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    /**
     * Copies all bytes to a string using UTF-8 encoding.
     *
     * @param in
     * @return String
     */
    public static String copyToString(InputStream in) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            copy(in, out);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    /**
     * @return true if the given string is not null and not empty
     */
    public static boolean isSet(@Nullable String s) {
        return s != null && !s.isEmpty();
    }

    public static String escapeJava(String text) {
        StringBuilder sb = null;
        for (int i = 0, n = text.length(); i < n; i++) {
            String repl = null;
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    repl = "\\\"";
                    break;
                case '\\':
                    repl = "\\\\";
                    break;
                case '\t':
                    repl = "\\t";
                    break;
                case '\n':
                    repl = "\\n";
                    break;
                case '\r':
                    repl = "\\r";
                    break;
                case '\b':
                    repl = "\\b";
                    break;
                case '\f':
                    repl = "\\f";
                    break;
                default:
                    if (sb != null) {
                        sb.append(c);
                    }
            }

            if (repl != null) {
                if (sb == null) {
                    sb = new StringBuilder(text.substring(0, i));
                }
                sb.append(repl);
            }
        }
        return sb != null ? sb.toString() : text;
    }

    /**
     * HTML/XML escape's the given text.
     *
     * @param text
     * @return Escaped text
     */
    public static String escapeHtmlXml(String text) {
        StringBuilder sb = null;
        for (int i = 0, n = text.length(); i < n; i++) {
            String repl = null;
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    repl = "&quot;";
                    break;
                case '&':
                    repl = "&amp;";
                    break;
                case '<':
                    repl = "&lt;";
                    break;
                case '>':
                    repl = "&gt;";
                    break;
                default:
                    if (sb != null) {
                        sb.append(c);
                    }
            }

            if (repl != null) {
                if (sb == null) {
                    sb = new StringBuilder(text.substring(0, i));
                }
                sb.append(repl);
            }
        }
        return sb != null ? sb.toString() : text;
    }

    /**
     * non-api
     */
    public static String getTemplate(Class<?> clazz) {
        Component cmp = clazz.getAnnotation(Component.class);
        if (cmp == null) {
            throw new NgoyException("Annotation %s not found on class %s", Component.class.getName(), clazz.getName());
        }
        String templateUrl = cmp.templateUrl();
        String tpl;
        if (isSet(templateUrl)) {
            InputStream in = clazz.getResourceAsStream(templateUrl);
            if (in == null) {
                throw new NgoyException("Template could not be found: '%s'", templateUrl);
            }
            try (InputStream inn = in) {
                tpl = copyToString(inn);
            } catch (Exception e) {
                throw wrap(e);
            }
        } else {
            tpl = cmp.template();
        }
        return tpl;
    }

    /**
     * Returns the line at the given line number.
     *
     * @param s          string with lines
     * @param lineNumber starting at 1
     * @return Line
     */
    public static String getLine(String s, int lineNumber) {
        try {
            LineNumberReader lnr = new LineNumberReader(new StringReader(s));
            int i = 1;
            String line;
            while ((line = lnr.readLine()) != null) {
                if (i == lineNumber) {
                    return line;
                }
                i++;
            }
            return format("<line %s not found>", lineNumber);
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    public static String getCompileExceptionMessageWithoutLocation(CompileException compileException) {
        String message = compileException.getMessage();
        if (compileException.getLocation() != null) {
            Matcher matcher = Pattern.compile(".*Column \\d*:(.*)")
                    .matcher(message);
            if (matcher.find()) {
                return matcher.group(1)
                        .trim();
            }
        }

        return message;
    }

    public static String primitiveToRefType(String clazz) {
        boolean primitive = isPrimitive(clazz);
        if (!primitive) {
            return clazz;
        }

        if ("char".equals(clazz)) {
            return Character.class.getSimpleName();
        } else if ("int".equals(clazz)) {
            return Integer.class.getSimpleName();
        }

        return clazz.substring(0, 1)
                .toUpperCase() + clazz.substring(1);
    }

    public static boolean isPrimitive(String clazz) {
        return "boolean".equals(clazz) //
                || "byte".equals(clazz) //
                || "char".equals(clazz) //
                || "short".equals(clazz) //
                || "int".equals(clazz) //
                || "long".equals(clazz) //
                || "float".equals(clazz) //
                || "double".equals(clazz);
    }

    private static String innerSourceClassName(Class<?> clazz) {
        String name = clazz.getName();
        Class<?> enclosingClass = clazz.getEnclosingClass();
        while (enclosingClass != null) {
            String enclosingName = enclosingClass.getName();
            String right = name.substring(enclosingName.length());
            if (right.charAt(0) == '$') {
                right = right.substring(1);
            }
            name = format("%s.%s", enclosingName, right);
            enclosingClass = enclosingClass.getEnclosingClass();
        }

        return name;
    }

    public static String sourceClassName(Class<?> clazz) {
        return sourceClassName(clazz, null);
    }

    public static String sourceClassName(Field field) {
        return sourceClassName(field.getType(), field.getGenericType());
    }

    public static String sourceClassName(Method method) {
        return sourceClassName(method.getReturnType(), method.getGenericReturnType());
    }

    public static String sourceClassName(Class<?> clazz, @Nullable Type genericType) {
        if (genericType != null) {
            return genericType.toString();
        }
        return clazz.isArray() ? format("%s[]", sourceClassName(clazz.getComponentType())) : innerSourceClassName(clazz);
    }

    public static String getArrayClass(String type) {
        switch (type) {
            case "byte":
                return "[B";
            case "boolean":
                return "[Z";
            case "char":
                return "[C";
            case "short":
                return "[S";
            case "int":
                return "[I";
            case "long":
                return "[J";
            case "float":
                return "[F";
            case "double":
                return "[D";
            default:
                return format("[L%s;", type);
        }
    }

    public static Class<?> tryLoadClass(String type) {
        Class<?> c = null;
        try {
            c = Class.forName(type);
        } catch (ClassNotFoundException e) {
            try {
                c = Thread.currentThread()
                        .getContextClassLoader()
                        .loadClass(type);
            } catch (ClassNotFoundException ee) {
                // ignore
            }
        }
        return c;
    }

    public static Method findMethod(Class<?> c, String methodName, int nArgs) {
        return findMethod(c, methodName, nArgs, false);
    }

    @Nullable
    public static Method findMethod(Class<?> c, String methodName, int nArgs, boolean forLambda) {
        boolean checkItf = c.isInterface() && forLambda;

        return Stream.of(c.getMethods())
                .filter(meth -> {
                    int mods = meth.getModifiers();
                    boolean staticc = Modifier.isStatic(mods);
                    if (checkItf && staticc) {
                        return false;
                    }
                    boolean hasArgs = meth.getParameterCount() == nArgs || meth.isVarArgs();
                    boolean isName = meth.getName()
                            .equals(methodName) || !isSet(methodName);
                    return !meth.isDefault() && hasArgs && isName && Modifier.isPublic(mods);
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the key/value pairs array as a map.
     *
     * @param keyValuePairs
     * @return Map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> map(Object... keyValuePairs) {
        Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0, n = keyValuePairs.length; i < n; i += 2) {
            map.put((K) keyValuePairs[i], (V) keyValuePairs[i + 1]);
        }
        return map;
    }

    /**
     * Returns the items as a list.
     *
     * @param items Items
     * @return List
     */
    @SafeVarargs
    public static <T> List<T> list(T... items) {
        return asList(items);
    }

    /**
     * Returns the items as a set.
     *
     * @param items Items
     * @return Set
     */
    @SafeVarargs
    public static <T> Set<T> set(T... items) {
        return new HashSet<>(list(items));
    }
}
