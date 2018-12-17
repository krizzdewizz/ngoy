package ngoy.core.internal;

import java.util.AbstractList;
import java.util.Iterator;

import ngoy.core.NgoyException;

@SuppressWarnings("rawtypes")
public class IteratorWithVariables implements Iterator {

	private final Iterator target;

	public int index;
	public boolean first;
	public boolean last;
	public boolean even;
	public boolean odd;

	/* @formatter:off */
	public IteratorWithVariables(boolean[] arr) {
		this(new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } });
	}
	public IteratorWithVariables(byte[] arr) {
		this(new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } });
	}
	public IteratorWithVariables(char[] arr) {
		this(new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } });
	}
	public IteratorWithVariables(short[] arr) {
		this(new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } });
	}
	public IteratorWithVariables(int[] arr) {
		this(new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } });
	}
	public IteratorWithVariables(long[] arr) {
		this(new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } });
	}
	public IteratorWithVariables(float[] arr) {
		this(new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } });
	}
	public IteratorWithVariables(double[] arr) {
		this(new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } });
	}
	public IteratorWithVariables(Object[] arr) {
		this(new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } });
	}
	/* @formatter:on */

	public IteratorWithVariables(Iterable iterable) {
		if (iterable == null) {
			throw new NgoyException("Cannot repeat with a null iterable");
		}
		this.target = iterable.iterator();
		index = -1;
	}

	@Override
	public boolean hasNext() {
		return target.hasNext();
	}

	@Override
	public Object next() {
		Object obj = target.next();

		index++;

		first = index == 0;
		last = !hasNext();
		even = (index % 2) == 0;
		odd = !even;

		return obj;
	}
}