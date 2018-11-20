package ngoy.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated classes can be used as a pipe inside an expression.
 * <p>
 * A pipe must implement the {@link PipeTransform} interface.
 * 
 * @author krizz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Pipe {
	/**
	 * @return the name of the pipe
	 */
	String value();
}
