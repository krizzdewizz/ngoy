package ngoy.hyperml;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * HTML Elements and attributes.
 * 
 * @author krizz
 */
public class Html extends Base<Html> {

	private static final Set<String> VOID_ELEMENTS = new HashSet<>(
			Arrays.asList("area", "base", "br", "col", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"));

	@Override
	protected boolean isVoidElement(String name) {
		return VOID_ELEMENTS.contains(name.toLowerCase());
	}

	@Override
	protected boolean escapeText() {
		String name = stack.peek();
		if (name == null) {
			return true;
		}
		String lower = name.toLowerCase();
		return !lower.equals("script") && !lower.equals("style");
	}

	public static final String fill = "100%";

	/**
	 * Outputs a CSS style declaration as a {@link #text(Object...)} call.
	 * 
	 * @param selector           The selector
	 * @param propertyValuePairs css property/value pairs such as
	 *                           <code>["color", "red", "display", "none"]</code>
	 */
	public Html css(String selector, Object... propertyValuePairs) {
		StringBuilder sb = new StringBuilder();
		sb.append(selector);
		sb.append('{');
		for (int i = 0, n = propertyValuePairs.length; i < n; i += 2) {
			sb.append(propertyValuePairs[i]);
			sb.append(':');
			sb.append(propertyValuePairs[i + 1]);
			sb.append(';');
		}
		sb.append('}');
		text(sb.toString());
		return this;
	}

	/**
	 * Returns a space delimited class list for the given map.
	 * <p>
	 * Entry key is the class name, which is added to the list if the entry value
	 * evaluates to true.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * Map&lt;String, Boolean&gt; classes = new HashMap&lt;&gt;();
	* classes.put(&quot;peter&quot;, true);
	* classes.put(&quot;paul&quot;, false);
	* classes.put(&quot;mary&quot;, Boolean.TRUE);
	*
	* a(classs, classes(classes), $);	
	* 
	 * &lt;a class="peter mary"&gt;&lt;/a&gt
	 * </pre>
	 * 
	 * @param classBooleanPairs [Object, Boolean] pairs
	 * @return class list
	 */
	public String classes(Map<? extends Object, Boolean> classBooleanPairs) {
		StringBuilder sb = new StringBuilder();
		for (Entry<? extends Object, Boolean> entry : classBooleanPairs.entrySet()) {
			if (entry.getValue()) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(entry.getKey());
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a space delimited class list for the given pairs.
	 * <p>
	 * First element is the class name (Object), which is added to the list if the
	 * second element (Boolean) evaluates to true.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * a(classs, classes("peter", true, "paul", false, "mary", Boolean.TRUE), $);
	 * 
	 * &lt;a class="peter mary"&gt;&lt;/a&gt
	 * </pre>
	 * 
	 * 
	 * @param classBooleanPairs [Object, Boolean] pairs
	 * @return class list
	 */
	public String classes(Object... classBooleanPairs) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, n = classBooleanPairs.length; i < n; i += 2) {
			Boolean pred = (Boolean) classBooleanPairs[i + 1];
			if (pred) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(classBooleanPairs[i]);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a semicolon delimited style list for the given map.
	 * <p>
	 * Entry key is the style name, which is added to the list if the entry value
	 * evaluates to a non-null, non-empty string.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * Map&lt;String, String&gt; styles = new HashMap&lt;&gt;();
	 * styles.put(&quot;background-color&quot;, &quot;red&quot;);
	 * styles.put(&quot;color&quot;, null);
	 * styles.put(&quot;display&quot;, &quot;none&quot;);
	 *
	 * a(style, styles(styles), $);
	 * 
	 * &lt;a style="background-color:red;display:none"&gt;&lt;/a&gt
	 * </pre>
	 * 
	 * @param styleValuePairs [Object, Object] pairs
	 * @return style list
	 */
	public String styles(Map<?, ?> styleValuePairs) {
		StringBuilder sb = new StringBuilder();
		for (Entry<?, ?> entry : styleValuePairs.entrySet()) {
			Object value = entry.getValue();
			if (value != null && !value.toString()
					.isEmpty()) {
				if (sb.length() > 0) {
					sb.append(';');
				}
				sb.append(entry.getKey());
				sb.append(':');
				sb.append(value);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a semicolon delimited style list for the given pairs.
	 * <p>
	 * First element is the style name (Object), which is added to the list if the
	 * second element, the style's value (Object) evaluates to a non-null, non-empty
	 * string.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * a(style, styles("background-color", "red", "color", null, "display", "none"), $);
	 * 
	 * &lt;a style="background-color:red;display:none"&gt;&lt;/a&gt
	 * </pre>
	 * 
	 * 
	 * @param styleValuePairs [Object, Object] pairs
	 * @return style list
	 */
	public String styles(Object... styleValuePairs) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, n = styleValuePairs.length; i < n; i += 2) {
			Object value = styleValuePairs[i + 1];
			if (value != null && !value.toString()
					.isEmpty()) {
				if (sb.length() > 0) {
					sb.append(';');
				}
				sb.append(styleValuePairs[i]);
				sb.append(':');
				sb.append(value);
			}
		}
		return sb.toString();
	}

	// begin generated code

	/**
	 * The <code>_blank</code> enumeration.
	 */
	public static final String _blank = "_blank";
	/**
	 * The <code>_parent</code> enumeration.
	 */
	public static final String _parent = "_parent";
	/**
	 * The <code>_self</code> enumeration.
	 */
	public static final String _self = "_self";
	/**
	 * The <code>_top</code> enumeration.
	 */
	public static final String _top = "_top";
	/**
	 * The <code>abbr</code> attribute.
	 */
	public static final String abbr = "abbr";
	/**
	 * The <code>above</code> enumeration.
	 */
	public static final String above = "above";
	/**
	 * The <code>accept</code> attribute.
	 */
	public static final String accept = "accept";
	/**
	 * The <code>accept-charset</code> attribute.
	 */
	public static final String acceptCharset = "accept-charset";
	/**
	 * The <code>accesskey</code> attribute.
	 */
	public static final String accesskey = "accesskey";
	/**
	 * The <code>action</code> attribute.
	 */
	public static final String action = "action";
	/**
	 * The <code>align</code> attribute.
	 */
	public static final String align = "align";
	/**
	 * The <code>all</code> enumeration.
	 */
	public static final String all = "all";
	/**
	 * The <code>allow-forms</code> enumeration.
	 */
	public static final String allowForms = "allow-forms";
	/**
	 * The <code>allow-same-origin</code> enumeration.
	 */
	public static final String allowSameOrigin = "allow-same-origin";
	/**
	 * The <code>allow-scripts</code> enumeration.
	 */
	public static final String allowScripts = "allow-scripts";
	/**
	 * The <code>allow-top-navigation</code> enumeration.
	 */
	public static final String allowTopNavigation = "allow-top-navigation";
	/**
	 * The <code>alt</code> attribute.
	 */
	public static final String alt = "alt";
	/**
	 * The <code>any</code> enumeration.
	 */
	public static final String any = "any";
	/**
	 * The <code>application/x-www-form-urlencoded</code> enumeration.
	 */
	public static final String applicationXWwwFormUrlencoded = "application/x-www-form-urlencoded";
	/**
	 * The <code>archive</code> attribute.
	 */
	public static final String archive = "archive";
	/**
	 * The <code>async</code> attribute.
	 */
	public static final String async = "async";
	/**
	 * The <code>auto</code> enumeration.
	 */
	public static final String auto = "auto";
	/**
	 * The <code>autocomplete</code> attribute.
	 */
	public static final String autocomplete = "autocomplete";
	/**
	 * The <code>autofocus</code> attribute.
	 */
	public static final String autofocus = "autofocus";
	/**
	 * The <code>autoplay</code> attribute.
	 */
	public static final String autoplay = "autoplay";
	/**
	 * The <code>axis</code> attribute.
	 */
	public static final String axis = "axis";
	/**
	 * The <code>baseline</code> enumeration.
	 */
	public static final String baseline = "baseline";
	/**
	 * The <code>below</code> enumeration.
	 */
	public static final String below = "below";
	/**
	 * The <code>border</code> attribute.
	 */
	public static final String border = "border";
	/**
	 * The <code>bottom</code> enumeration.
	 */
	public static final String bottom = "bottom";
	/**
	 * The <code>box</code> enumeration.
	 */
	public static final String box = "box";
	/**
	 * The <code>button</code> enumeration.
	 */
	public static final String button = "button";
	/**
	 * The <code>captions</code> enumeration.
	 */
	public static final String captions = "captions";
	/**
	 * The <code>cellpadding</code> attribute.
	 */
	public static final String cellpadding = "cellpadding";
	/**
	 * The <code>cellspacing</code> attribute.
	 */
	public static final String cellspacing = "cellspacing";
	/**
	 * The <code>center</code> enumeration.
	 */
	public static final String center = "center";
	/**
	 * The <code>challenge</code> attribute.
	 */
	public static final String challenge = "challenge";
	/**
	 * The <code>chapters</code> enumeration.
	 */
	public static final String chapters = "chapters";
	/**
	 * The <code>char</code> attribute.
	 */
	public static final String charr = "char";
	/**
	 * The <code>charoff</code> attribute.
	 */
	public static final String charoff = "charoff";
	/**
	 * The <code>charset</code> attribute.
	 */
	public static final String charset = "charset";
	/**
	 * The <code>checkbox</code> enumeration.
	 */
	public static final String checkbox = "checkbox";
	/**
	 * The <code>checked</code> attribute.
	 */
	public static final String checked = "checked";
	/**
	 * The <code>circle</code> enumeration.
	 */
	public static final String circle = "circle";
	/**
	 * The <code>cite</code> attribute.
	 */
	public static final String cite = "cite";
	/**
	 * The <code>class</code> attribute.
	 */
	public static final String classs = "class";
	/**
	 * The <code>classid</code> attribute.
	 */
	public static final String classid = "classid";
	/**
	 * The <code>codebase</code> attribute.
	 */
	public static final String codebase = "codebase";
	/**
	 * The <code>codetype</code> attribute.
	 */
	public static final String codetype = "codetype";
	/**
	 * The <code>col</code> enumeration.
	 */
	public static final String col = "col";
	/**
	 * The <code>colgroup</code> enumeration.
	 */
	public static final String colgroup = "colgroup";
	/**
	 * The <code>color</code> enumeration.
	 */
	public static final String color = "color";
	/**
	 * The <code>cols</code> attribute.
	 */
	public static final String cols = "cols";
	/**
	 * The <code>colspan</code> attribute.
	 */
	public static final String colspan = "colspan";
	/**
	 * The <code>command</code> enumeration.
	 */
	public static final String command = "command";
	/**
	 * The <code>content</code> attribute.
	 */
	public static final String content = "content";
	/**
	 * The <code>content-language</code> enumeration.
	 */
	public static final String contentLanguage = "content-language";
	/**
	 * The <code>content-type</code> enumeration.
	 */
	public static final String contentType = "content-type";
	/**
	 * The <code>contenteditable</code> attribute.
	 */
	public static final String contenteditable = "contenteditable";
	/**
	 * The <code>context</code> enumeration.
	 */
	public static final String context = "context";
	/**
	 * The <code>contextmenu</code> attribute.
	 */
	public static final String contextmenu = "contextmenu";
	/**
	 * The <code>controls</code> attribute.
	 */
	public static final String controls = "controls";
	/**
	 * The <code>coords</code> attribute.
	 */
	public static final String coords = "coords";
	/**
	 * The <code>copy</code> enumeration.
	 */
	public static final String copy = "copy";
	/**
	 * The <code>data</code> attribute.
	 */
	public static final String data = "data";
	/**
	 * The <code>date</code> enumeration.
	 */
	public static final String date = "date";
	/**
	 * The <code>datetime</code> attribute.
	 */
	public static final String datetime = "datetime";
	/**
	 * The <code>datetime-local</code> enumeration.
	 */
	public static final String datetimeLocal = "datetime-local";
	/**
	 * The <code>declare</code> attribute.
	 */
	public static final String declare = "declare";
	/**
	 * The <code>default</code> attribute.
	 */
	public static final String defaultt = "default";
	/**
	 * The <code>default-style</code> enumeration.
	 */
	public static final String defaultStyle = "default-style";
	/**
	 * The <code>defer</code> attribute.
	 */
	public static final String defer = "defer";
	/**
	 * The <code>descriptions</code> enumeration.
	 */
	public static final String descriptions = "descriptions";
	/**
	 * The <code>dir</code> attribute.
	 */
	public static final String dir = "dir";
	/**
	 * The <code>dirname</code> attribute.
	 */
	public static final String dirname = "dirname";
	/**
	 * The <code>disabled</code> attribute.
	 */
	public static final String disabled = "disabled";
	/**
	 * The <code>draggable</code> attribute.
	 */
	public static final String draggable = "draggable";
	/**
	 * The <code>dropzone</code> attribute.
	 */
	public static final String dropzone = "dropzone";
	/**
	 * The <code>email</code> enumeration.
	 */
	public static final String email = "email";
	/**
	 * The <code>enctype</code> attribute.
	 */
	public static final String enctype = "enctype";
	/**
	 * The <code>false</code> enumeration.
	 */
	public static final String falsee = "false";
	/**
	 * The <code>file</code> enumeration.
	 */
	public static final String file = "file";
	/**
	 * The <code>for</code> attribute.
	 */
	public static final String forr = "for";
	/**
	 * The <code>form</code> attribute.
	 */
	public static final String form = "form";
	/**
	 * The <code>formaction</code> attribute.
	 */
	public static final String formaction = "formaction";
	/**
	 * The <code>formenctype</code> attribute.
	 */
	public static final String formenctype = "formenctype";
	/**
	 * The <code>formmethod</code> attribute.
	 */
	public static final String formmethod = "formmethod";
	/**
	 * The <code>formnovalidate</code> attribute.
	 */
	public static final String formnovalidate = "formnovalidate";
	/**
	 * The <code>formtarget</code> attribute.
	 */
	public static final String formtarget = "formtarget";
	/**
	 * The <code>frame</code> attribute.
	 */
	public static final String frame = "frame";
	/**
	 * The <code>get</code> enumeration.
	 */
	public static final String get = "get";
	/**
	 * The <code>groups</code> enumeration.
	 */
	public static final String groups = "groups";
	/**
	 * The <code>hard</code> enumeration.
	 */
	public static final String hard = "hard";
	/**
	 * The <code>headers</code> attribute.
	 */
	public static final String headers = "headers";
	/**
	 * The <code>height</code> attribute.
	 */
	public static final String height = "height";
	/**
	 * The <code>hidden</code> attribute.
	 */
	public static final String hidden = "hidden";
	/**
	 * The <code>high</code> attribute.
	 */
	public static final String high = "high";
	/**
	 * The <code>href</code> attribute.
	 */
	public static final String href = "href";
	/**
	 * The <code>hreflang</code> attribute.
	 */
	public static final String hreflang = "hreflang";
	/**
	 * The <code>hsides</code> enumeration.
	 */
	public static final String hsides = "hsides";
	/**
	 * The <code>http-equiv</code> attribute.
	 */
	public static final String httpEquiv = "http-equiv";
	/**
	 * The <code>icon</code> attribute.
	 */
	public static final String icon = "icon";
	/**
	 * The <code>id</code> attribute.
	 */
	public static final String id = "id";
	/**
	 * The <code>image</code> enumeration.
	 */
	public static final String image = "image";
	/**
	 * The <code>ismap</code> attribute.
	 */
	public static final String ismap = "ismap";
	/**
	 * The <code>justify</code> enumeration.
	 */
	public static final String justify = "justify";
	/**
	 * The <code>keytype</code> attribute.
	 */
	public static final String keytype = "keytype";
	/**
	 * The <code>kind</code> attribute.
	 */
	public static final String kind = "kind";
	/**
	 * The <code>label</code> attribute.
	 */
	public static final String label = "label";
	/**
	 * The <code>lang</code> attribute.
	 */
	public static final String lang = "lang";
	/**
	 * The <code>left</code> enumeration.
	 */
	public static final String left = "left";
	/**
	 * The <code>lhs</code> enumeration.
	 */
	public static final String lhs = "lhs";
	/**
	 * The <code>link</code> enumeration.
	 */
	public static final String link = "link";
	/**
	 * The <code>list</code> attribute.
	 */
	public static final String list = "list";
	/**
	 * The <code>longdesc</code> attribute.
	 */
	public static final String longdesc = "longdesc";
	/**
	 * The <code>loop</code> attribute.
	 */
	public static final String loop = "loop";
	/**
	 * The <code>low</code> attribute.
	 */
	public static final String low = "low";
	/**
	 * The <code>ltr</code> enumeration.
	 */
	public static final String ltr = "ltr";
	/**
	 * The <code>manifest</code> attribute.
	 */
	public static final String manifest = "manifest";
	/**
	 * The <code>max</code> attribute.
	 */
	public static final String max = "max";
	/**
	 * The <code>maxlength</code> attribute.
	 */
	public static final String maxlength = "maxlength";
	/**
	 * The <code>media</code> attribute.
	 */
	public static final String media = "media";
	/**
	 * The <code>mediagroup</code> attribute.
	 */
	public static final String mediagroup = "mediagroup";
	/**
	 * The <code>metadata</code> enumeration.
	 */
	public static final String metadata = "metadata";
	/**
	 * The <code>method</code> attribute.
	 */
	public static final String method = "method";
	/**
	 * The <code>middle</code> enumeration.
	 */
	public static final String middle = "middle";
	/**
	 * The <code>min</code> attribute.
	 */
	public static final String min = "min";
	/**
	 * The <code>month</code> enumeration.
	 */
	public static final String month = "month";
	/**
	 * The <code>move</code> enumeration.
	 */
	public static final String move = "move";
	/**
	 * The <code>multipart/form-data</code> enumeration.
	 */
	public static final String multipartFormData = "multipart/form-data";
	/**
	 * The <code>multiple</code> attribute.
	 */
	public static final String multiple = "multiple";
	/**
	 * The <code>muted</code> attribute.
	 */
	public static final String muted = "muted";
	/**
	 * The <code>name</code> attribute.
	 */
	public static final String name = "name";
	/**
	 * The <code>nohref</code> attribute.
	 */
	public static final String nohref = "nohref";
	/**
	 * The <code>none</code> enumeration.
	 */
	public static final String none = "none";
	/**
	 * The <code>novalidate</code> attribute.
	 */
	public static final String novalidate = "novalidate";
	/**
	 * The <code>number</code> enumeration.
	 */
	public static final String number = "number";
	/**
	 * The <code>object</code> enumeration.
	 */
	public static final String object = "object";
	/**
	 * The <code>off</code> enumeration.
	 */
	public static final String off = "off";
	/**
	 * The <code>on</code> enumeration.
	 */
	public static final String on = "on";
	/**
	 * The <code>onabort</code> attribute.
	 */
	public static final String onabort = "onabort";
	/**
	 * The <code>onafterprint</code> attribute.
	 */
	public static final String onafterprint = "onafterprint";
	/**
	 * The <code>onbeforeprint</code> attribute.
	 */
	public static final String onbeforeprint = "onbeforeprint";
	/**
	 * The <code>onbeforeunload</code> attribute.
	 */
	public static final String onbeforeunload = "onbeforeunload";
	/**
	 * The <code>onblur</code> attribute.
	 */
	public static final String onblur = "onblur";
	/**
	 * The <code>oncanplay</code> attribute.
	 */
	public static final String oncanplay = "oncanplay";
	/**
	 * The <code>oncanplaythrough</code> attribute.
	 */
	public static final String oncanplaythrough = "oncanplaythrough";
	/**
	 * The <code>onchange</code> attribute.
	 */
	public static final String onchange = "onchange";
	/**
	 * The <code>onclick</code> attribute.
	 */
	public static final String onclick = "onclick";
	/**
	 * The <code>oncontextmenu</code> attribute.
	 */
	public static final String oncontextmenu = "oncontextmenu";
	/**
	 * The <code>ondblclick</code> attribute.
	 */
	public static final String ondblclick = "ondblclick";
	/**
	 * The <code>ondrag</code> attribute.
	 */
	public static final String ondrag = "ondrag";
	/**
	 * The <code>ondragend</code> attribute.
	 */
	public static final String ondragend = "ondragend";
	/**
	 * The <code>ondragenter</code> attribute.
	 */
	public static final String ondragenter = "ondragenter";
	/**
	 * The <code>ondragleave</code> attribute.
	 */
	public static final String ondragleave = "ondragleave";
	/**
	 * The <code>ondragover</code> attribute.
	 */
	public static final String ondragover = "ondragover";
	/**
	 * The <code>ondragstart</code> attribute.
	 */
	public static final String ondragstart = "ondragstart";
	/**
	 * The <code>ondrop</code> attribute.
	 */
	public static final String ondrop = "ondrop";
	/**
	 * The <code>ondurationchange</code> attribute.
	 */
	public static final String ondurationchange = "ondurationchange";
	/**
	 * The <code>onemptied</code> attribute.
	 */
	public static final String onemptied = "onemptied";
	/**
	 * The <code>onended</code> attribute.
	 */
	public static final String onended = "onended";
	/**
	 * The <code>onerror</code> attribute.
	 */
	public static final String onerror = "onerror";
	/**
	 * The <code>onfocus</code> attribute.
	 */
	public static final String onfocus = "onfocus";
	/**
	 * The <code>onhashchange</code> attribute.
	 */
	public static final String onhashchange = "onhashchange";
	/**
	 * The <code>oninput</code> attribute.
	 */
	public static final String oninput = "oninput";
	/**
	 * The <code>oninvalid</code> attribute.
	 */
	public static final String oninvalid = "oninvalid";
	/**
	 * The <code>onkeydown</code> attribute.
	 */
	public static final String onkeydown = "onkeydown";
	/**
	 * The <code>onkeypress</code> attribute.
	 */
	public static final String onkeypress = "onkeypress";
	/**
	 * The <code>onkeyup</code> attribute.
	 */
	public static final String onkeyup = "onkeyup";
	/**
	 * The <code>onload</code> attribute.
	 */
	public static final String onload = "onload";
	/**
	 * The <code>onloadeddata</code> attribute.
	 */
	public static final String onloadeddata = "onloadeddata";
	/**
	 * The <code>onloadedmetadata</code> attribute.
	 */
	public static final String onloadedmetadata = "onloadedmetadata";
	/**
	 * The <code>onloadstart</code> attribute.
	 */
	public static final String onloadstart = "onloadstart";
	/**
	 * The <code>onmessage</code> attribute.
	 */
	public static final String onmessage = "onmessage";
	/**
	 * The <code>onmousedown</code> attribute.
	 */
	public static final String onmousedown = "onmousedown";
	/**
	 * The <code>onmousemove</code> attribute.
	 */
	public static final String onmousemove = "onmousemove";
	/**
	 * The <code>onmouseout</code> attribute.
	 */
	public static final String onmouseout = "onmouseout";
	/**
	 * The <code>onmouseover</code> attribute.
	 */
	public static final String onmouseover = "onmouseover";
	/**
	 * The <code>onmouseup</code> attribute.
	 */
	public static final String onmouseup = "onmouseup";
	/**
	 * The <code>onmousewheel</code> attribute.
	 */
	public static final String onmousewheel = "onmousewheel";
	/**
	 * The <code>onoffline</code> attribute.
	 */
	public static final String onoffline = "onoffline";
	/**
	 * The <code>ononline</code> attribute.
	 */
	public static final String ononline = "ononline";
	/**
	 * The <code>onpause</code> attribute.
	 */
	public static final String onpause = "onpause";
	/**
	 * The <code>onplay</code> attribute.
	 */
	public static final String onplay = "onplay";
	/**
	 * The <code>onplaying</code> attribute.
	 */
	public static final String onplaying = "onplaying";
	/**
	 * The <code>onpopstate</code> attribute.
	 */
	public static final String onpopstate = "onpopstate";
	/**
	 * The <code>onprogress</code> attribute.
	 */
	public static final String onprogress = "onprogress";
	/**
	 * The <code>onratechange</code> attribute.
	 */
	public static final String onratechange = "onratechange";
	/**
	 * The <code>onreadystatechange</code> attribute.
	 */
	public static final String onreadystatechange = "onreadystatechange";
	/**
	 * The <code>onredo</code> attribute.
	 */
	public static final String onredo = "onredo";
	/**
	 * The <code>onreset</code> attribute.
	 */
	public static final String onreset = "onreset";
	/**
	 * The <code>onresize</code> attribute.
	 */
	public static final String onresize = "onresize";
	/**
	 * The <code>onscroll</code> attribute.
	 */
	public static final String onscroll = "onscroll";
	/**
	 * The <code>onseeked</code> attribute.
	 */
	public static final String onseeked = "onseeked";
	/**
	 * The <code>onseeking</code> attribute.
	 */
	public static final String onseeking = "onseeking";
	/**
	 * The <code>onselect</code> attribute.
	 */
	public static final String onselect = "onselect";
	/**
	 * The <code>onshow</code> attribute.
	 */
	public static final String onshow = "onshow";
	/**
	 * The <code>onstalled</code> attribute.
	 */
	public static final String onstalled = "onstalled";
	/**
	 * The <code>onstorage</code> attribute.
	 */
	public static final String onstorage = "onstorage";
	/**
	 * The <code>onsubmit</code> attribute.
	 */
	public static final String onsubmit = "onsubmit";
	/**
	 * The <code>onsuspend</code> attribute.
	 */
	public static final String onsuspend = "onsuspend";
	/**
	 * The <code>ontimeupdate</code> attribute.
	 */
	public static final String ontimeupdate = "ontimeupdate";
	/**
	 * The <code>onundo</code> attribute.
	 */
	public static final String onundo = "onundo";
	/**
	 * The <code>onunload</code> attribute.
	 */
	public static final String onunload = "onunload";
	/**
	 * The <code>onvolumechange</code> attribute.
	 */
	public static final String onvolumechange = "onvolumechange";
	/**
	 * The <code>onwaiting</code> attribute.
	 */
	public static final String onwaiting = "onwaiting";
	/**
	 * The <code>open</code> attribute.
	 */
	public static final String open = "open";
	/**
	 * The <code>optimum</code> attribute.
	 */
	public static final String optimum = "optimum";
	/**
	 * The <code>password</code> enumeration.
	 */
	public static final String password = "password";
	/**
	 * The <code>pattern</code> attribute.
	 */
	public static final String pattern = "pattern";
	/**
	 * The <code>placeholder</code> attribute.
	 */
	public static final String placeholder = "placeholder";
	/**
	 * The <code>poly</code> enumeration.
	 */
	public static final String poly = "poly";
	/**
	 * The <code>post</code> enumeration.
	 */
	public static final String post = "post";
	/**
	 * The <code>poster</code> attribute.
	 */
	public static final String poster = "poster";
	/**
	 * The <code>preload</code> attribute.
	 */
	public static final String preload = "preload";
	/**
	 * The <code>profile</code> attribute.
	 */
	public static final String profile = "profile";
	/**
	 * The <code>pubdate</code> enumeration.
	 */
	public static final String pubdate = "pubdate";
	/**
	 * The <code>radio</code> enumeration.
	 */
	public static final String radio = "radio";
	/**
	 * The <code>radiogroup</code> attribute.
	 */
	public static final String radiogroup = "radiogroup";
	/**
	 * The <code>range</code> enumeration.
	 */
	public static final String range = "range";
	/**
	 * The <code>readonly</code> attribute.
	 */
	public static final String readonly = "readonly";
	/**
	 * The <code>rect</code> enumeration.
	 */
	public static final String rect = "rect";
	/**
	 * The <code>ref</code> enumeration.
	 */
	public static final String ref = "ref";
	/**
	 * The <code>refresh</code> enumeration.
	 */
	public static final String refresh = "refresh";
	/**
	 * The <code>rel</code> attribute.
	 */
	public static final String rel = "rel";
	/**
	 * The <code>required</code> attribute.
	 */
	public static final String required = "required";
	/**
	 * The <code>reset</code> enumeration.
	 */
	public static final String reset = "reset";
	/**
	 * The <code>rev</code> attribute.
	 */
	public static final String rev = "rev";
	/**
	 * The <code>reversed</code> attribute.
	 */
	public static final String reversed = "reversed";
	/**
	 * The <code>rhs</code> enumeration.
	 */
	public static final String rhs = "rhs";
	/**
	 * The <code>right</code> enumeration.
	 */
	public static final String right = "right";
	/**
	 * The <code>row</code> enumeration.
	 */
	public static final String row = "row";
	/**
	 * The <code>rowgroup</code> enumeration.
	 */
	public static final String rowgroup = "rowgroup";
	/**
	 * The <code>rows</code> attribute.
	 */
	public static final String rows = "rows";
	/**
	 * The <code>rowspan</code> attribute.
	 */
	public static final String rowspan = "rowspan";
	/**
	 * The <code>rsa</code> enumeration.
	 */
	public static final String rsa = "rsa";
	/**
	 * The <code>rtl</code> enumeration.
	 */
	public static final String rtl = "rtl";
	/**
	 * The <code>rules</code> attribute.
	 */
	public static final String rules = "rules";
	/**
	 * The <code>sandbox</code> attribute.
	 */
	public static final String sandbox = "sandbox";
	/**
	 * The <code>scheme</code> attribute.
	 */
	public static final String scheme = "scheme";
	/**
	 * The <code>scope</code> attribute.
	 */
	public static final String scope = "scope";
	/**
	 * The <code>scoped</code> attribute.
	 */
	public static final String scoped = "scoped";
	/**
	 * The <code>seamless</code> attribute.
	 */
	public static final String seamless = "seamless";
	/**
	 * The <code>search</code> enumeration.
	 */
	public static final String search = "search";
	/**
	 * The <code>selected</code> attribute.
	 */
	public static final String selected = "selected";
	/**
	 * The <code>set-cookie</code> enumeration.
	 */
	public static final String setCookie = "set-cookie";
	/**
	 * The <code>shape</code> attribute.
	 */
	public static final String shape = "shape";
	/**
	 * The <code>size</code> attribute.
	 */
	public static final String size = "size";
	/**
	 * The <code>sizes</code> attribute.
	 */
	public static final String sizes = "sizes";
	/**
	 * The <code>soft</code> enumeration.
	 */
	public static final String soft = "soft";
	/**
	 * The <code>span</code> attribute.
	 */
	public static final String span = "span";
	/**
	 * The <code>spellcheck</code> attribute.
	 */
	public static final String spellcheck = "spellcheck";
	/**
	 * The <code>src</code> attribute.
	 */
	public static final String src = "src";
	/**
	 * The <code>srcdoc</code> attribute.
	 */
	public static final String srcdoc = "srcdoc";
	/**
	 * The <code>srclang</code> attribute.
	 */
	public static final String srclang = "srclang";
	/**
	 * The <code>standby</code> attribute.
	 */
	public static final String standby = "standby";
	/**
	 * The <code>start</code> attribute.
	 */
	public static final String start = "start";
	/**
	 * The <code>step</code> attribute.
	 */
	public static final String step = "step";
	/**
	 * The <code>style</code> attribute.
	 */
	public static final String style = "style";
	/**
	 * The <code>submit</code> enumeration.
	 */
	public static final String submit = "submit";
	/**
	 * The <code>subtitles</code> enumeration.
	 */
	public static final String subtitles = "subtitles";
	/**
	 * The <code>summary</code> attribute.
	 */
	public static final String summary = "summary";
	/**
	 * The <code>tabindex</code> attribute.
	 */
	public static final String tabindex = "tabindex";
	/**
	 * The <code>target</code> attribute.
	 */
	public static final String target = "target";
	/**
	 * The <code>tel</code> enumeration.
	 */
	public static final String tel = "tel";
	/**
	 * The <code>text</code> enumeration.
	 */
	public static final String text = "text";
	/**
	 * The <code>text/plain</code> enumeration.
	 */
	public static final String textPlain = "text/plain";
	/**
	 * The <code>time</code> enumeration.
	 */
	public static final String time = "time";
	/**
	 * The <code>title</code> attribute.
	 */
	public static final String title = "title";
	/**
	 * The <code>toolbar</code> enumeration.
	 */
	public static final String toolbar = "toolbar";
	/**
	 * The <code>top</code> enumeration.
	 */
	public static final String top = "top";
	/**
	 * The <code>true</code> enumeration.
	 */
	public static final String truee = "true";
	/**
	 * The <code>type</code> attribute.
	 */
	public static final String type = "type";
	/**
	 * The <code>url</code> enumeration.
	 */
	public static final String url = "url";
	/**
	 * The <code>usemap</code> attribute.
	 */
	public static final String usemap = "usemap";
	/**
	 * The <code>valign</code> attribute.
	 */
	public static final String valign = "valign";
	/**
	 * The <code>value</code> attribute.
	 */
	public static final String value = "value";
	/**
	 * The <code>valuetype</code> attribute.
	 */
	public static final String valuetype = "valuetype";
	/**
	 * The <code>void</code> enumeration.
	 */
	public static final String voidd = "void";
	/**
	 * The <code>vsides</code> enumeration.
	 */
	public static final String vsides = "vsides";
	/**
	 * The <code>week</code> enumeration.
	 */
	public static final String week = "week";
	/**
	 * The <code>width</code> attribute.
	 */
	public static final String width = "width";
	/**
	 * The <code>wrap</code> attribute.
	 */
	public static final String wrap = "wrap";

	/**
	 * The <code>a</code> element.
	 * 
	 * @return this
	 */
	public Html a(Object... params) {
		return $("a", params);
	}

	/**
	 * The <code>abbr</code> element.
	 * 
	 * @return this
	 */
	public Html abbr(Object... params) {
		return $("abbr", params);
	}

	/**
	 * The <code>acronym</code> element.
	 * 
	 * @return this
	 */
	public Html acronym(Object... params) {
		return $("acronym", params);
	}

	/**
	 * The <code>address</code> element.
	 * 
	 * @return this
	 */
	public Html address(Object... params) {
		return $("address", params);
	}

	/**
	 * The <code>area</code> element.
	 * 
	 * @return this
	 */
	public Html area(Object... params) {
		return $("area", params);
	}

	/**
	 * The <code>article</code> element.
	 * 
	 * @return this
	 */
	public Html article(Object... params) {
		return $("article", params);
	}

	/**
	 * The <code>aside</code> element.
	 * 
	 * @return this
	 */
	public Html aside(Object... params) {
		return $("aside", params);
	}

	/**
	 * The <code>audio</code> element.
	 * 
	 * @return this
	 */
	public Html audio(Object... params) {
		return $("audio", params);
	}

	/**
	 * The <code>b</code> element.
	 * 
	 * @return this
	 */
	public Html b(Object... params) {
		return $("b", params);
	}

	/**
	 * The <code>base</code> element.
	 * 
	 * @return this
	 */
	public Html base(Object... params) {
		return $("base", params);
	}

	/**
	 * The <code>bdi</code> element.
	 * 
	 * @return this
	 */
	public Html bdi(Object... params) {
		return $("bdi", params);
	}

	/**
	 * The <code>bdo</code> element.
	 * 
	 * @return this
	 */
	public Html bdo(Object... params) {
		return $("bdo", params);
	}

	/**
	 * The <code>big</code> element.
	 * 
	 * @return this
	 */
	public Html big(Object... params) {
		return $("big", params);
	}

	/**
	 * The <code>blockquote</code> element.
	 * 
	 * @return this
	 */
	public Html blockquote(Object... params) {
		return $("blockquote", params);
	}

	/**
	 * The <code>body</code> element.
	 * 
	 * @return this
	 */
	public Html body(Object... params) {
		return $("body", params);
	}

	/**
	 * The <code>br</code> element.
	 * 
	 * @return this
	 */
	public Html br(Object... params) {
		return $("br", params);
	}

	/**
	 * The <code>button</code> element.
	 * 
	 * @return this
	 */
	public Html button(Object... params) {
		return $("button", params);
	}

	/**
	 * The <code>canvas</code> element.
	 * 
	 * @return this
	 */
	public Html canvas(Object... params) {
		return $("canvas", params);
	}

	/**
	 * The <code>caption</code> element.
	 * 
	 * @return this
	 */
	public Html caption(Object... params) {
		return $("caption", params);
	}

	/**
	 * The <code>cite</code> element.
	 * 
	 * @return this
	 */
	public Html cite(Object... params) {
		return $("cite", params);
	}

	/**
	 * The <code>code</code> element.
	 * 
	 * @return this
	 */
	public Html code(Object... params) {
		return $("code", params);
	}

	/**
	 * The <code>col</code> element.
	 * 
	 * @return this
	 */
	public Html col(Object... params) {
		return $("col", params);
	}

	/**
	 * The <code>colgroup</code> element.
	 * 
	 * @return this
	 */
	public Html colgroup(Object... params) {
		return $("colgroup", params);
	}

	/**
	 * The <code>command</code> element.
	 * 
	 * @return this
	 */
	public Html command(Object... params) {
		return $("command", params);
	}

	/**
	 * The <code>datalist</code> element.
	 * 
	 * @return this
	 */
	public Html datalist(Object... params) {
		return $("datalist", params);
	}

	/**
	 * The <code>dd</code> element.
	 * 
	 * @return this
	 */
	public Html dd(Object... params) {
		return $("dd", params);
	}

	/**
	 * The <code>del</code> element.
	 * 
	 * @return this
	 */
	public Html del(Object... params) {
		return $("del", params);
	}

	/**
	 * The <code>details</code> element.
	 * 
	 * @return this
	 */
	public Html details(Object... params) {
		return $("details", params);
	}

	/**
	 * The <code>dfn</code> element.
	 * 
	 * @return this
	 */
	public Html dfn(Object... params) {
		return $("dfn", params);
	}

	/**
	 * The <code>div</code> element.
	 * 
	 * @return this
	 */
	public Html div(Object... params) {
		return $("div", params);
	}

	/**
	 * The <code>dl</code> element.
	 * 
	 * @return this
	 */
	public Html dl(Object... params) {
		return $("dl", params);
	}

	/**
	 * The <code>dt</code> element.
	 * 
	 * @return this
	 */
	public Html dt(Object... params) {
		return $("dt", params);
	}

	/**
	 * The <code>em</code> element.
	 * 
	 * @return this
	 */
	public Html em(Object... params) {
		return $("em", params);
	}

	/**
	 * The <code>embed</code> element.
	 * 
	 * @return this
	 */
	public Html embed(Object... params) {
		return $("embed", params);
	}

	/**
	 * The <code>fieldset</code> element.
	 * 
	 * @return this
	 */
	public Html fieldset(Object... params) {
		return $("fieldset", params);
	}

	/**
	 * The <code>figcaption</code> element.
	 * 
	 * @return this
	 */
	public Html figcaption(Object... params) {
		return $("figcaption", params);
	}

	/**
	 * The <code>figure</code> element.
	 * 
	 * @return this
	 */
	public Html figure(Object... params) {
		return $("figure", params);
	}

	/**
	 * The <code>footer</code> element.
	 * 
	 * @return this
	 */
	public Html footer(Object... params) {
		return $("footer", params);
	}

	/**
	 * The <code>form</code> element.
	 * 
	 * @return this
	 */
	public Html form(Object... params) {
		return $("form", params);
	}

	/**
	 * The <code>h1</code> element.
	 * 
	 * @return this
	 */
	public Html h1(Object... params) {
		return $("h1", params);
	}

	/**
	 * The <code>h2</code> element.
	 * 
	 * @return this
	 */
	public Html h2(Object... params) {
		return $("h2", params);
	}

	/**
	 * The <code>h3</code> element.
	 * 
	 * @return this
	 */
	public Html h3(Object... params) {
		return $("h3", params);
	}

	/**
	 * The <code>h4</code> element.
	 * 
	 * @return this
	 */
	public Html h4(Object... params) {
		return $("h4", params);
	}

	/**
	 * The <code>h5</code> element.
	 * 
	 * @return this
	 */
	public Html h5(Object... params) {
		return $("h5", params);
	}

	/**
	 * The <code>h6</code> element.
	 * 
	 * @return this
	 */
	public Html h6(Object... params) {
		return $("h6", params);
	}

	/**
	 * The <code>head</code> element.
	 * 
	 * @return this
	 */
	public Html head(Object... params) {
		return $("head", params);
	}

	/**
	 * The <code>header</code> element.
	 * 
	 * @return this
	 */
	public Html header(Object... params) {
		return $("header", params);
	}

	/**
	 * The <code>hgroup</code> element.
	 * 
	 * @return this
	 */
	public Html hgroup(Object... params) {
		return $("hgroup", params);
	}

	/**
	 * The <code>hr</code> element.
	 * 
	 * @return this
	 */
	public Html hr(Object... params) {
		return $("hr", params);
	}

	/**
	 * The <code>html</code> element.
	 * 
	 * @return this
	 */
	public Html html(Object... params) {
		return $("html", params);
	}

	/**
	 * The <code>i</code> element.
	 * 
	 * @return this
	 */
	public Html i(Object... params) {
		return $("i", params);
	}

	/**
	 * The <code>iframe</code> element.
	 * 
	 * @return this
	 */
	public Html iframe(Object... params) {
		return $("iframe", params);
	}

	/**
	 * The <code>img</code> element.
	 * 
	 * @return this
	 */
	public Html img(Object... params) {
		return $("img", params);
	}

	/**
	 * The <code>input</code> element.
	 * 
	 * @return this
	 */
	public Html input(Object... params) {
		return $("input", params);
	}

	/**
	 * The <code>ins</code> element.
	 * 
	 * @return this
	 */
	public Html ins(Object... params) {
		return $("ins", params);
	}

	/**
	 * The <code>kbd</code> element.
	 * 
	 * @return this
	 */
	public Html kbd(Object... params) {
		return $("kbd", params);
	}

	/**
	 * The <code>keygen</code> element.
	 * 
	 * @return this
	 */
	public Html keygen(Object... params) {
		return $("keygen", params);
	}

	/**
	 * The <code>label</code> element.
	 * 
	 * @return this
	 */
	public Html label(Object... params) {
		return $("label", params);
	}

	/**
	 * The <code>legend</code> element.
	 * 
	 * @return this
	 */
	public Html legend(Object... params) {
		return $("legend", params);
	}

	/**
	 * The <code>li</code> element.
	 * 
	 * @return this
	 */
	public Html li(Object... params) {
		return $("li", params);
	}

	/**
	 * The <code>link</code> element.
	 * 
	 * @return this
	 */
	public Html link(Object... params) {
		return $("link", params);
	}

	/**
	 * The <code>map</code> element.
	 * 
	 * @return this
	 */
	public Html map(Object... params) {
		return $("map", params);
	}

	/**
	 * The <code>mark</code> element.
	 * 
	 * @return this
	 */
	public Html mark(Object... params) {
		return $("mark", params);
	}

	/**
	 * The <code>menu</code> element.
	 * 
	 * @return this
	 */
	public Html menu(Object... params) {
		return $("menu", params);
	}

	/**
	 * The <code>meta</code> element.
	 * 
	 * @return this
	 */
	public Html meta(Object... params) {
		return $("meta", params);
	}

	/**
	 * The <code>meter</code> element.
	 * 
	 * @return this
	 */
	public Html meter(Object... params) {
		return $("meter", params);
	}

	/**
	 * The <code>nav</code> element.
	 * 
	 * @return this
	 */
	public Html nav(Object... params) {
		return $("nav", params);
	}

	/**
	 * The <code>noscript</code> element.
	 * 
	 * @return this
	 */
	public Html noscript(Object... params) {
		return $("noscript", params);
	}

	/**
	 * The <code>object</code> element.
	 * 
	 * @return this
	 */
	public Html object(Object... params) {
		return $("object", params);
	}

	/**
	 * The <code>ol</code> element.
	 * 
	 * @return this
	 */
	public Html ol(Object... params) {
		return $("ol", params);
	}

	/**
	 * The <code>optgroup</code> element.
	 * 
	 * @return this
	 */
	public Html optgroup(Object... params) {
		return $("optgroup", params);
	}

	/**
	 * The <code>option</code> element.
	 * 
	 * @return this
	 */
	public Html option(Object... params) {
		return $("option", params);
	}

	/**
	 * The <code>output</code> element.
	 * 
	 * @return this
	 */
	public Html output(Object... params) {
		return $("output", params);
	}

	/**
	 * The <code>p</code> element.
	 * 
	 * @return this
	 */
	public Html p(Object... params) {
		return $("p", params);
	}

	/**
	 * The <code>param</code> element.
	 * 
	 * @return this
	 */
	public Html param(Object... params) {
		return $("param", params);
	}

	/**
	 * The <code>pre</code> element.
	 * 
	 * @return this
	 */
	public Html pre(Object... params) {
		return $("pre", params);
	}

	/**
	 * The <code>progress</code> element.
	 * 
	 * @return this
	 */
	public Html progress(Object... params) {
		return $("progress", params);
	}

	/**
	 * The <code>q</code> element.
	 * 
	 * @return this
	 */
	public Html q(Object... params) {
		return $("q", params);
	}

	/**
	 * The <code>rp</code> element.
	 * 
	 * @return this
	 */
	public Html rp(Object... params) {
		return $("rp", params);
	}

	/**
	 * The <code>rt</code> element.
	 * 
	 * @return this
	 */
	public Html rt(Object... params) {
		return $("rt", params);
	}

	/**
	 * The <code>ruby</code> element.
	 * 
	 * @return this
	 */
	public Html ruby(Object... params) {
		return $("ruby", params);
	}

	/**
	 * The <code>s</code> element.
	 * 
	 * @return this
	 */
	public Html s(Object... params) {
		return $("s", params);
	}

	/**
	 * The <code>samp</code> element.
	 * 
	 * @return this
	 */
	public Html samp(Object... params) {
		return $("samp", params);
	}

	/**
	 * The <code>script</code> element.
	 * 
	 * @return this
	 */
	public Html script(Object... params) {
		return $("script", params);
	}

	/**
	 * The <code>section</code> element.
	 * 
	 * @return this
	 */
	public Html section(Object... params) {
		return $("section", params);
	}

	/**
	 * The <code>select</code> element.
	 * 
	 * @return this
	 */
	public Html select(Object... params) {
		return $("select", params);
	}

	/**
	 * The <code>small</code> element.
	 * 
	 * @return this
	 */
	public Html small(Object... params) {
		return $("small", params);
	}

	/**
	 * The <code>source</code> element.
	 * 
	 * @return this
	 */
	public Html source(Object... params) {
		return $("source", params);
	}

	/**
	 * The <code>span</code> element.
	 * 
	 * @return this
	 */
	public Html span(Object... params) {
		return $("span", params);
	}

	/**
	 * The <code>strong</code> element.
	 * 
	 * @return this
	 */
	public Html strong(Object... params) {
		return $("strong", params);
	}

	/**
	 * The <code>style</code> element.
	 * 
	 * @return this
	 */
	public Html style(Object... params) {
		return $("style", params);
	}

	/**
	 * The <code>sub</code> element.
	 * 
	 * @return this
	 */
	public Html sub(Object... params) {
		return $("sub", params);
	}

	/**
	 * The <code>summary</code> element.
	 * 
	 * @return this
	 */
	public Html summary(Object... params) {
		return $("summary", params);
	}

	/**
	 * The <code>sup</code> element.
	 * 
	 * @return this
	 */
	public Html sup(Object... params) {
		return $("sup", params);
	}

	/**
	 * The <code>table</code> element.
	 * 
	 * @return this
	 */
	public Html table(Object... params) {
		return $("table", params);
	}

	/**
	 * The <code>tbody</code> element.
	 * 
	 * @return this
	 */
	public Html tbody(Object... params) {
		return $("tbody", params);
	}

	/**
	 * The <code>td</code> element.
	 * 
	 * @return this
	 */
	public Html td(Object... params) {
		return $("td", params);
	}

	/**
	 * The <code>textarea</code> element.
	 * 
	 * @return this
	 */
	public Html textarea(Object... params) {
		return $("textarea", params);
	}

	/**
	 * The <code>tfoot</code> element.
	 * 
	 * @return this
	 */
	public Html tfoot(Object... params) {
		return $("tfoot", params);
	}

	/**
	 * The <code>th</code> element.
	 * 
	 * @return this
	 */
	public Html th(Object... params) {
		return $("th", params);
	}

	/**
	 * The <code>thead</code> element.
	 * 
	 * @return this
	 */
	public Html thead(Object... params) {
		return $("thead", params);
	}

	/**
	 * The <code>time</code> element.
	 * 
	 * @return this
	 */
	public Html time(Object... params) {
		return $("time", params);
	}

	/**
	 * The <code>title</code> element.
	 * 
	 * @return this
	 */
	public Html title(Object... params) {
		return $("title", params);
	}

	/**
	 * The <code>tr</code> element.
	 * 
	 * @return this
	 */
	public Html tr(Object... params) {
		return $("tr", params);
	}

	/**
	 * The <code>track</code> element.
	 * 
	 * @return this
	 */
	public Html track(Object... params) {
		return $("track", params);
	}

	/**
	 * The <code>tt</code> element.
	 * 
	 * @return this
	 */
	public Html tt(Object... params) {
		return $("tt", params);
	}

	/**
	 * The <code>u</code> element.
	 * 
	 * @return this
	 */
	public Html u(Object... params) {
		return $("u", params);
	}

	/**
	 * The <code>ul</code> element.
	 * 
	 * @return this
	 */
	public Html ul(Object... params) {
		return $("ul", params);
	}

	/**
	 * The <code>var</code> element.
	 * 
	 * @return this
	 */
	public Html var(Object... params) {
		return $("var", params);
	}

	/**
	 * The <code>video</code> element.
	 * 
	 * @return this
	 */
	public Html video(Object... params) {
		return $("video", params);
	}

	/**
	 * The <code>wbr</code> element.
	 * 
	 * @return this
	 */
	public Html wbr(Object... params) {
		return $("wbr", params);
	}
}
