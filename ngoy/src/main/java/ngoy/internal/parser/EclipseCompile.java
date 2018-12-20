package ngoy.internal.parser;

import static java.util.Arrays.asList;
import static ngoy.core.NgoyException.wrap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import ngoy.core.NgoyException;

public class EclipseCompile {

	static JavaCompiler javac;
	static SpecialJavaFileManager fileManager;
	static SpecialClassLoader cl;
	static {
		javac = new EclipseCompiler();
		StandardJavaFileManager sjfm = javac.getStandardFileManager(null, null, null);
		cl = new SpecialClassLoader();
		fileManager = new SpecialJavaFileManager(sjfm, cl);
	}

	public static Class<?> compileClass(String code, String className) {
		try {
			List<String> options = asList("-1.8");

			List<JavaFileObject> compilationUnits = asList(new MemorySource(className, code));
			StringWriter out = new StringWriter();
			JavaCompiler.CompilationTask compile = javac.getTask(out, fileManager, null, options, null, compilationUnits);
			boolean res = compile.call();
			if (res) {
				return cl.findClass(className);
			} else {
				throw new NgoyException(out.toString());
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}

class MemorySource extends SimpleJavaFileObject {
	private String src;

	public MemorySource(String name, String src) {
		super(URI.create("file:///" + name.replace('.', '/') + ".java"), Kind.SOURCE);
		this.src = src;
	}

	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return src;
	}

	public OutputStream openOutputStream() {
		throw new IllegalStateException();
	}

	public InputStream openInputStream() {
		return new ByteArrayInputStream(src.getBytes());
	}
}

class SpecialJavaFileManager extends ForwardingJavaFileManager {
	private SpecialClassLoader xcl;

	public SpecialJavaFileManager(StandardJavaFileManager sjfm, SpecialClassLoader xcl) {
		super(sjfm);
		this.xcl = xcl;
	}

	public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
		MemoryByteCode mbc = new MemoryByteCode(name);
		xcl.addClass(name, mbc);
		return mbc;
	}

	public ClassLoader getClassLoader(Location location) {
		return xcl;
	}
}

class MemoryByteCode extends SimpleJavaFileObject {
	private ByteArrayOutputStream baos;

	public MemoryByteCode(String name) {
		super(URI.create("byte:///" + name + ".class"), Kind.CLASS);
	}

	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		throw new IllegalStateException();
	}

	public OutputStream openOutputStream() {
		baos = new ByteArrayOutputStream();
		return baos;
	}

	public InputStream openInputStream() {
		throw new IllegalStateException();
	}

	public byte[] getBytes() {
		return baos.toByteArray();
	}
}

class SpecialClassLoader extends ClassLoader {
	private Map<String, MemoryByteCode> m = new HashMap<String, MemoryByteCode>();

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		MemoryByteCode mbc = m.get(name);
		if (mbc == null) {
			mbc = m.get(name.replace(".", "/"));
			if (mbc == null) {
				return super.findClass(name);
			}
		}
		return defineClass(name, mbc.getBytes(), 0, mbc.getBytes().length);
	}

	public void addClass(String name, MemoryByteCode mbc) {
		m.put(name, mbc);
	}
}