package ngoy.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An input parameter of a component.
 * <p>
 * An annotated field or setter method will receive the value through an
 * attribute binding on the component markup.
 * <p>
 * The annotated field must be public, non-final, non-static.<br>
 * The annotated setter method must be public, non-static and must have exactly
 * one parameter.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;Component(selector = "person", template = "hello {{title}} {{name}}")
 * public class PersonCmp {
 * 	&#64;Input()
 * 	public String name;
 *
 * 	public String title;
 * 	&#64;Input()
 * 	public void setTitle(String title) {
 * 		this.title = title;
 * 		// do something more
 *    }
 * }
 *
 * &#64;Component(selector = "test", template = "&lt;person name="Peter" [title]="'Sir'"&gt;&lt;/person>")
 * &#64;NgModule(declarations = { PersonCmp.class })
 * public static class TestCmp {
 * }
 * </pre>
 * <p>
 * will produce:
 *
 * <pre>
 * &lt;person&gt;hello Sir Peter&lt;/person&gt;
 * </pre>
 *
 * @author krizz
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Input {
    /**
     * The name of the input variable or empty to use the annotated field's name.
     *
     * @return name of the input
     */
    String value() default "";
}
