package ngoy.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bind an expression to an attribute, class, style or text on the
 * component/directive's host element.
 * 
 * @author krizz
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface HostBinding {
	/**
	 * An <b>attr</b> binding binds an expression to the value of an attribute.
	 * <p>
	 * If the expression evaluates to <code>null</code>, the attribute is not
	 * written.
	 * <p>
	 * 
	 * <pre>
	 * &#64;Component(selector = "person", template = "")
	 * public class PersonCmp {
	 * 
	 * 	&#64;HostBinding("attr.age")
	 * 	public int age = 22;
	 * }
	 * </pre>
	 * 
	 * Output:
	 * 
	 * <pre>
	 * &lt;person age="22"&gt;&lt;/person&gt;
	 * </pre>
	 * <p>
	 * A <b>Class</b> binding is a special treatment for the <code>class</code>
	 * attribute.
	 * <p>
	 * An existing class list will be preserved.
	 * <p>
	 * If none of the bindings evaluates to true, the <code>class</code> attribute
	 * is not written.
	 * <p>
	 * 
	 * <pre>
	 * &#64;Component(selector = "person", template = "")
	 * public class PersonCmp {
	 * 
	 * 	&#64;HostBinding("class.person-very-important")
	 * 	public boolean vip = true;
	 * 
	 * 	&#64;HostBinding("class.person-famous")
	 * 	public boolean famous = true;
	 * }
	 * </pre>
	 * 
	 * Output:
	 * 
	 * <pre>
	 * &lt;person class="person-very-important person-famous"&gt;&lt;/person&gt;
	 * </pre>
	 * <p>
	 * A <b>style</b> bindings is a special treatment for the <code>style</code>
	 * attribute.
	 * <p>
	 * An existing style list will be preserved.
	 * <p>
	 * If none of the bindings evaluates to true, the <code>style</code> attribute
	 * is not written.
	 * <p>
	 * 
	 * <pre>
	 * &#64;Component(selector = "person", template = "")
	 * public class PersonCmp {
	 * 
	 * 	&#64;HostBinding("style.color")
	 * 	public String vip = "red";
	 * 
	 * 	&#64;HostBinding("style.height.cm")
	 * 	public int sizeInCm = 182;
	 * }
	 * </pre>
	 * 
	 * Output:
	 * 
	 * <pre>
	 * &lt;person style="color:red;height:182cm"&gt;&lt;/person&gt;
	 * </pre>
	 * <p>
	 * 
	 * An <b>text</b> binding binds an expression to the text content of the
	 * element.
	 * <p>
	 * If more than one text binding is encountered, it is undetermined, which one
	 * wins.
	 * 
	 * <pre>
	 * &#64;Component(selector = "person", template = "")
	 * public class PersonCmp {
	 * 
	 * 	&#64;HostBinding("text")
	 * 	public String name = "Peter";
	 * }
	 * </pre>
	 * 
	 * Output:
	 * 
	 * <pre>
	 * &lt;person&gt;Peter&lt;/person&gt;
	 * </pre>
	 * 
	 * @return one of <code>class.x</code>, <code>style.x</code>,
	 *         <code>attr.x</code> or <code>text</code>
	 */
	String value();
}
