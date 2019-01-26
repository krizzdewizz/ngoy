package ngoy.hyperml;

import ngoy.hyperml.base.XmlBase;

/**
 * Writes arbitrary xml with only the method {@link #$(Object, Object...)}.
 * <p>
 * <code>$()</code> expects its parameters as follows:
 * <p>
 * Example:
 * 
 * <pre>
 * $("html");
 * {
 *   $("body", "onload", "doThings()");               // attribute name-value pairs
 *   {
 *      $("h1", "class", "title", "hello world", $);  // with text content, $ --&gt; 'short close' 
 *   }
 *   $(); // body                                     // no parameters --&gt; end element
 * }
 * $(); // html
 * 
 * --&gt;
 * 
 * &lt;html&gt;
 *   &lt;body onload=&quot;doThings()&quot;&gt;
 *     &lt;h1 class=&quot;title&quot;&gt;hello world&lt;/h1&gt;
 *   &lt;/body&gt;
 * &lt;/html&gt;
 * </pre>
 * 
 * @author krizzdewizz
 */
public class Xml extends XmlBase<Xml> {
}