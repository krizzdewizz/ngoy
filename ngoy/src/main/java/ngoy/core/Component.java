package ngoy.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ngoy.Ngoy.Builder;
import ngoy.Ngoy.Config;

/**
 * A component is 'logic' for HTML/XML elements.
 * <p>
 * A component is bound to an element by it's {@link #selector()}. Whenever an
 * element is encountered that matches this selector, the component takes care
 * of the element's subtree/content.
 * <p>
 * The element's content is basically replaced by the component's
 * {@link #template()} or {@link #templateUrl()}.
 * 
 * @author krizz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
	/**
	 * The component will be associated to all elements matching this CSS selector.
	 * <p>
	 * The selector for the root app component may be empty.
	 * 
	 * @return CSS selector
	 */
	String selector();

	/**
	 * Resource URL for the template.
	 * 
	 * @return URL
	 * @see Class#getResourceAsStream(String)
	 */
	String templateUrl() default "";

	/**
	 * The template's content as an inline string.
	 * 
	 * @return template
	 */
	String template() default "";

	/**
	 * CSS styles resource urls.
	 * <p>
	 * Styles are inlined into the template upon compilation. Either in first
	 * existing &lt;style&gt; element or into a new one.
	 * 
	 * @return Style URLs
	 * @see Class#getResourceAsStream(String)
	 */
	String[] styleUrls() default {};

	/**
	 * CSS styles.
	 * <p>
	 * Styles are inlined into the template upon compilation. Either in first
	 * existing &lt;style&gt; element or into a new one.
	 * 
	 * @return Styles
	 */
	String[] styles() default {};

	/**
	 * Providers/services that this component depends on.
	 * 
	 * @return providers
	 */
	Class<?>[] providers() default {};

	/**
	 * Providers/services that this component depends on.
	 * 
	 * @return providers
	 */
	Provide[] provide() default {};

	/**
	 * The output content type. Applies only for the root app component.
	 * 
	 * @return content type
	 * @see Builder#contentType(String)
	 * @see Config#contentType
	 */
	String contentType() default "";

}
