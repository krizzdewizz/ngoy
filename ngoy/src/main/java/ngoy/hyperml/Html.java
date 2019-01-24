package ngoy.hyperml;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Elements and attributes contained in <code>xhtml1-strict.xsd.dat</code>. <br>
 * 
 * @author krizz
 */
public class Html extends Base<Html> {

	private static final Set<String> VOID_ELEMENTS = new HashSet<>(Arrays.asList("area", "base", "br", "col", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"));

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
	 * The <code>headers</code> attribute.
	 */
	public static final String headers = "headers";
	/**
	 * The <code>summary</code> attribute.
	 */
	public static final String summary = "summary";
	/**
	 * The <code>cols</code> attribute.
	 */
	public static final String cols = "cols";
	/**
	 * The <code>char</code> attribute.
	 */
	public static final String charr = "char";
	/**
	 * The <code>for</code> attribute.
	 */
	public static final String forr = "for";
	/**
	 * The <code>accept</code> attribute.
	 */
	public static final String accept = "accept";
	/**
	 * The <code>cellpadding</code> attribute.
	 */
	public static final String cellpadding = "cellpadding";
	/**
	 * The <code>type</code> attribute.
	 */
	public static final String type = "type";
	/**
	 * The <code>cellspacing</code> attribute.
	 */
	public static final String cellspacing = "cellspacing";
	/**
	 * The <code>accept-charset</code> attribute.
	 */
	public static final String acceptCharset = "accept-charset";
	/**
	 * The <code>hreflang</code> attribute.
	 */
	public static final String hreflang = "hreflang";
	/**
	 * The <code>maxlength</code> attribute.
	 */
	public static final String maxlength = "maxlength";
	/**
	 * The <code>height</code> attribute.
	 */
	public static final String height = "height";
	/**
	 * The <code>scheme</code> attribute.
	 */
	public static final String scheme = "scheme";
	/**
	 * The <code>onblur</code> attribute.
	 */
	public static final String onblur = "onblur";
	/**
	 * The <code>action</code> attribute.
	 */
	public static final String action = "action";
	/**
	 * The <code>value</code> attribute.
	 */
	public static final String value = "value";
	/**
	 * The <code>border</code> attribute.
	 */
	public static final String border = "border";
	/**
	 * The <code>href</code> attribute.
	 */
	public static final String href = "href";
	/**
	 * The <code>colspan</code> attribute.
	 */
	public static final String colspan = "colspan";
	/**
	 * The <code>ondblclick</code> attribute.
	 */
	public static final String ondblclick = "ondblclick";
	/**
	 * The <code>onmouseout</code> attribute.
	 */
	public static final String onmouseout = "onmouseout";
	/**
	 * The <code>align</code> attribute.
	 */
	public static final String align = "align";
	/**
	 * The <code>width</code> attribute.
	 */
	public static final String width = "width";
	/**
	 * The <code>abbr</code> attribute.
	 */
	public static final String abbr = "abbr";
	/**
	 * The <code>onkeyup</code> attribute.
	 */
	public static final String onkeyup = "onkeyup";
	/**
	 * The <code>class</code> attribute.
	 */
	public static final String classs = "class";
	/**
	 * The <code>charset</code> attribute.
	 */
	public static final String charset = "charset";
	/**
	 * The <code>label</code> attribute.
	 */
	public static final String label = "label";
	/**
	 * The <code>onfocus</code> attribute.
	 */
	public static final String onfocus = "onfocus";
	/**
	 * The <code>rowspan</code> attribute.
	 */
	public static final String rowspan = "rowspan";
	/**
	 * The <code>shape</code> attribute.
	 */
	public static final String shape = "shape";
	/**
	 * The <code>longdesc</code> attribute.
	 */
	public static final String longdesc = "longdesc";
	/**
	 * The <code>rows</code> attribute.
	 */
	public static final String rows = "rows";
	/**
	 * The <code>size</code> attribute.
	 */
	public static final String size = "size";
	/**
	 * The <code>onreset</code> attribute.
	 */
	public static final String onreset = "onreset";
	/**
	 * The <code>declare</code> attribute.
	 */
	public static final String declare = "declare";
	/**
	 * The <code>content</code> attribute.
	 */
	public static final String content = "content";
	/**
	 * The <code>cite</code> attribute.
	 */
	public static final String cite = "cite";
	/**
	 * The <code>frame</code> attribute.
	 */
	public static final String frame = "frame";
	/**
	 * The <code>onselect</code> attribute.
	 */
	public static final String onselect = "onselect";
	/**
	 * The <code>standby</code> attribute.
	 */
	public static final String standby = "standby";
	/**
	 * The <code>onmousedown</code> attribute.
	 */
	public static final String onmousedown = "onmousedown";
	/**
	 * The <code>media</code> attribute.
	 */
	public static final String media = "media";
	/**
	 * The <code>nohref</code> attribute.
	 */
	public static final String nohref = "nohref";
	/**
	 * The <code>span</code> attribute.
	 */
	public static final String span = "span";
	/**
	 * The <code>rev</code> attribute.
	 */
	public static final String rev = "rev";
	/**
	 * The <code>scope</code> attribute.
	 */
	public static final String scope = "scope";
	/**
	 * The <code>usemap</code> attribute.
	 */
	public static final String usemap = "usemap";
	/**
	 * The <code>onunload</code> attribute.
	 */
	public static final String onunload = "onunload";
	/**
	 * The <code>data</code> attribute.
	 */
	public static final String data = "data";
	/**
	 * The <code>space</code> attribute.
	 */
	public static final String space = "space";
	/**
	 * The <code>lang</code> attribute.
	 */
	public static final String lang = "lang";
	/**
	 * The <code>accesskey</code> attribute.
	 */
	public static final String accesskey = "accesskey";
	/**
	 * The <code>http-equiv</code> attribute.
	 */
	public static final String httpEquiv = "http-equiv";
	/**
	 * The <code>id</code> attribute.
	 */
	public static final String id = "id";
	/**
	 * The <code>valuetype</code> attribute.
	 */
	public static final String valuetype = "valuetype";
	/**
	 * The <code>defer</code> attribute.
	 */
	public static final String defer = "defer";
	/**
	 * The <code>selected</code> attribute.
	 */
	public static final String selected = "selected";
	/**
	 * The <code>ismap</code> attribute.
	 */
	public static final String ismap = "ismap";
	/**
	 * The <code>title</code> attribute.
	 */
	public static final String title = "title";
	/**
	 * The <code>style</code> attribute.
	 */
	public static final String style = "style";
	/**
	 * The <code>dir</code> attribute.
	 */
	public static final String dir = "dir";
	/**
	 * The <code>alt</code> attribute.
	 */
	public static final String alt = "alt";
	/**
	 * The <code>enctype</code> attribute.
	 */
	public static final String enctype = "enctype";
	/**
	 * The <code>name</code> attribute.
	 */
	public static final String name = "name";
	/**
	 * The <code>onmouseup</code> attribute.
	 */
	public static final String onmouseup = "onmouseup";
	/**
	 * The <code>src</code> attribute.
	 */
	public static final String src = "src";
	/**
	 * The <code>datetime</code> attribute.
	 */
	public static final String datetime = "datetime";
	/**
	 * The <code>multiple</code> attribute.
	 */
	public static final String multiple = "multiple";
	/**
	 * The <code>profile</code> attribute.
	 */
	public static final String profile = "profile";
	/**
	 * The <code>classid</code> attribute.
	 */
	public static final String classid = "classid";
	/**
	 * The <code>codetype</code> attribute.
	 */
	public static final String codetype = "codetype";
	/**
	 * The <code>axis</code> attribute.
	 */
	public static final String axis = "axis";
	/**
	 * The <code>onmousemove</code> attribute.
	 */
	public static final String onmousemove = "onmousemove";
	/**
	 * The <code>charoff</code> attribute.
	 */
	public static final String charoff = "charoff";
	/**
	 * The <code>tabindex</code> attribute.
	 */
	public static final String tabindex = "tabindex";
	/**
	 * The <code>onkeydown</code> attribute.
	 */
	public static final String onkeydown = "onkeydown";
	/**
	 * The <code>onkeypress</code> attribute.
	 */
	public static final String onkeypress = "onkeypress";
	/**
	 * The <code>rules</code> attribute.
	 */
	public static final String rules = "rules";
	/**
	 * The <code>onchange</code> attribute.
	 */
	public static final String onchange = "onchange";
	/**
	 * The <code>onsubmit</code> attribute.
	 */
	public static final String onsubmit = "onsubmit";
	/**
	 * The <code>onmouseover</code> attribute.
	 */
	public static final String onmouseover = "onmouseover";
	/**
	 * The <code>coords</code> attribute.
	 */
	public static final String coords = "coords";
	/**
	 * The <code>onload</code> attribute.
	 */
	public static final String onload = "onload";
	/**
	 * The <code>onclick</code> attribute.
	 */
	public static final String onclick = "onclick";
	/**
	 * The <code>method</code> attribute.
	 */
	public static final String method = "method";
	/**
	 * The <code>rel</code> attribute.
	 */
	public static final String rel = "rel";
	/**
	 * The <code>archive</code> attribute.
	 */
	public static final String archive = "archive";
	/**
	 * The <code>valign</code> attribute.
	 */
	public static final String valign = "valign";
	/**
	 * The <code>readonly</code> attribute.
	 */
	public static final String readonly = "readonly";
	/**
	 * The <code>checked</code> attribute.
	 */
	public static final String checked = "checked";
	/**
	 * The <code>disabled</code> attribute.
	 */
	public static final String disabled = "disabled";
	/**
	 * The <code>codebase</code> attribute.
	 */
	public static final String codebase = "codebase";

	public Html() {
	}

	/**
	 * Outputs a CSS style declaration as a {@link #text(Object...)} call.
	 * 
	 * @param selector
	 *            The selector
	 * @param propertyValuePairs
	 *            css property/value pairs such as
	 *            <code>["color", "red", "display", "none"]</code>
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
	 * The <code>html</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html html(Object... params) {
		$("html", params);
		return this;
	}

	/**
	 * The <code>head</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html head(Object... params) {
		$("head", params);
		return this;
	}

	/**
	 * The <code>title</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html title(Object... params) {
		$("title", params);
		return this;
	}

	/**
	 * The <code>base</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html base(Object... params) {
		$("base", params);
		return this;
	}

	/**
	 * The <code>meta</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html meta(Object... params) {
		$("meta", params);
		return this;
	}

	/**
	 * The <code>link</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html link(Object... params) {
		$("link", params);
		return this;
	}

	/**
	 * The <code>style</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html style(Object... params) {
		$("style", params);
		return this;
	}

	/**
	 * The <code>script</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html script(Object... params) {
		$("script", params);
		return this;
	}

	/**
	 * The <code>noscript</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html noscript(Object... params) {
		$("noscript", params);
		return this;
	}

	/**
	 * The <code>body</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html body(Object... params) {
		$("body", params);
		return this;
	}

	/**
	 * The <code>div</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html div(Object... params) {
		$("div", params);
		return this;
	}

	/**
	 * The <code>p</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html p(Object... params) {
		$("p", params);
		return this;
	}

	/**
	 * The <code>h1</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html h1(Object... params) {
		$("h1", params);
		return this;
	}

	/**
	 * The <code>h2</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html h2(Object... params) {
		$("h2", params);
		return this;
	}

	/**
	 * The <code>h3</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html h3(Object... params) {
		$("h3", params);
		return this;
	}

	/**
	 * The <code>h4</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html h4(Object... params) {
		$("h4", params);
		return this;
	}

	/**
	 * The <code>h5</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html h5(Object... params) {
		$("h5", params);
		return this;
	}

	/**
	 * The <code>h6</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html h6(Object... params) {
		$("h6", params);
		return this;
	}

	/**
	 * The <code>ul</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html ul(Object... params) {
		$("ul", params);
		return this;
	}

	/**
	 * The <code>ol</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html ol(Object... params) {
		$("ol", params);
		return this;
	}

	/**
	 * The <code>li</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html li(Object... params) {
		$("li", params);
		return this;
	}

	/**
	 * The <code>dl</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html dl(Object... params) {
		$("dl", params);
		return this;
	}

	/**
	 * The <code>dt</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html dt(Object... params) {
		$("dt", params);
		return this;
	}

	/**
	 * The <code>dd</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html dd(Object... params) {
		$("dd", params);
		return this;
	}

	/**
	 * The <code>address</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html address(Object... params) {
		$("address", params);
		return this;
	}

	/**
	 * The <code>hr</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html hr(Object... params) {
		$("hr", params);
		return this;
	}

	/**
	 * The <code>pre</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html pre(Object... params) {
		$("pre", params);
		return this;
	}

	/**
	 * The <code>blockquote</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html blockquote(Object... params) {
		$("blockquote", params);
		return this;
	}

	/**
	 * The <code>ins</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html ins(Object... params) {
		$("ins", params);
		return this;
	}

	/**
	 * The <code>del</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html del(Object... params) {
		$("del", params);
		return this;
	}

	/**
	 * The <code>a</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html a(Object... params) {
		$("a", params);
		return this;
	}

	/**
	 * The <code>span</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html span(Object... params) {
		$("span", params);
		return this;
	}

	/**
	 * The <code>bdo</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html bdo(Object... params) {
		$("bdo", params);
		return this;
	}

	/**
	 * The <code>br</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html br(Object... params) {
		$("br", params);
		return this;
	}

	/**
	 * The <code>em</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html em(Object... params) {
		$("em", params);
		return this;
	}

	/**
	 * The <code>strong</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html strong(Object... params) {
		$("strong", params);
		return this;
	}

	/**
	 * The <code>dfn</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html dfn(Object... params) {
		$("dfn", params);
		return this;
	}

	/**
	 * The <code>code</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html code(Object... params) {
		$("code", params);
		return this;
	}

	/**
	 * The <code>samp</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html samp(Object... params) {
		$("samp", params);
		return this;
	}

	/**
	 * The <code>kbd</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html kbd(Object... params) {
		$("kbd", params);
		return this;
	}

	/**
	 * The <code>var</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html var(Object... params) {
		$("var", params);
		return this;
	}

	/**
	 * The <code>cite</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html cite(Object... params) {
		$("cite", params);
		return this;
	}

	/**
	 * The <code>abbr</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html abbr(Object... params) {
		$("abbr", params);
		return this;
	}

	/**
	 * The <code>acronym</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html acronym(Object... params) {
		$("acronym", params);
		return this;
	}

	/**
	 * The <code>q</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html q(Object... params) {
		$("q", params);
		return this;
	}

	/**
	 * The <code>sub</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html sub(Object... params) {
		$("sub", params);
		return this;
	}

	/**
	 * The <code>sup</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html sup(Object... params) {
		$("sup", params);
		return this;
	}

	/**
	 * The <code>tt</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html tt(Object... params) {
		$("tt", params);
		return this;
	}

	/**
	 * The <code>i</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html i(Object... params) {
		$("i", params);
		return this;
	}

	/**
	 * The <code>b</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html b(Object... params) {
		$("b", params);
		return this;
	}

	/**
	 * The <code>big</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html big(Object... params) {
		$("big", params);
		return this;
	}

	/**
	 * The <code>small</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html small(Object... params) {
		$("small", params);
		return this;
	}

	/**
	 * The <code>object</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html object(Object... params) {
		$("object", params);
		return this;
	}

	/**
	 * The <code>param</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html param(Object... params) {
		$("param", params);
		return this;
	}

	/**
	 * The <code>img</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html img(Object... params) {
		$("img", params);
		return this;
	}

	/**
	 * The <code>map</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html map(Object... params) {
		$("map", params);
		return this;
	}

	/**
	 * The <code>area</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html area(Object... params) {
		$("area", params);
		return this;
	}

	/**
	 * The <code>form</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html form(Object... params) {
		$("form", params);
		return this;
	}

	/**
	 * The <code>label</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html label(Object... params) {
		$("label", params);
		return this;
	}

	/**
	 * The <code>input</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html input(Object... params) {
		$("input", params);
		return this;
	}

	/**
	 * The <code>select</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html select(Object... params) {
		$("select", params);
		return this;
	}

	/**
	 * The <code>optgroup</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html optgroup(Object... params) {
		$("optgroup", params);
		return this;
	}

	/**
	 * The <code>option</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html option(Object... params) {
		$("option", params);
		return this;
	}

	/**
	 * The <code>textarea</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html textarea(Object... params) {
		$("textarea", params);
		return this;
	}

	/**
	 * The <code>fieldset</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html fieldset(Object... params) {
		$("fieldset", params);
		return this;
	}

	/**
	 * The <code>legend</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html legend(Object... params) {
		$("legend", params);
		return this;
	}

	/**
	 * The <code>button</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html button(Object... params) {
		$("button", params);
		return this;
	}

	/**
	 * The <code>table</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html table(Object... params) {
		$("table", params);
		return this;
	}

	/**
	 * The <code>caption</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html caption(Object... params) {
		$("caption", params);
		return this;
	}

	/**
	 * The <code>thead</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html thead(Object... params) {
		$("thead", params);
		return this;
	}

	/**
	 * The <code>tfoot</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html tfoot(Object... params) {
		$("tfoot", params);
		return this;
	}

	/**
	 * The <code>tbody</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html tbody(Object... params) {
		$("tbody", params);
		return this;
	}

	/**
	 * The <code>colgroup</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html colgroup(Object... params) {
		$("colgroup", params);
		return this;
	}

	/**
	 * The <code>col</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html col(Object... params) {
		$("col", params);
		return this;
	}

	/**
	 * The <code>tr</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html tr(Object... params) {
		$("tr", params);
		return this;
	}

	/**
	 * The <code>th</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html th(Object... params) {
		$("th", params);
		return this;
	}

	/**
	 * The <code>td</code> element.
	 * 
	 * @param params
	 *            Parameters
	 * @see Xml#$(String, Object...)
	 */
	public Html td(Object... params) {
		$("td", params);
		return this;
	}
}
