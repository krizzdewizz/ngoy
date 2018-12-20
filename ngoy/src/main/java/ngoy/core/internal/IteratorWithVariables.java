package ngoy.core.internal;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.stream.Stream;

import ngoy.core.NgoyException;

@SuppressWarnings("rawtypes")
public class IteratorWithVariables implements Iterator {

	private static Iterable checkNonNull(Object obj, Iterable iter) {
		if (obj == null) {
			throw new NgoyException("Cannot repeat with a null iterable");
		}
		return iter;
	}

	private final Iterator target;

	public int index;
	public boolean first;
	public boolean last;
	public boolean even;
	public boolean odd;

	/* @formatter:off */
	public IteratorWithVariables(boolean[] arr) {
		this(checkNonNull(arr, new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } }));
	}
	public IteratorWithVariables(byte[] arr) {
		this(checkNonNull(arr,new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } }));
	}
	public IteratorWithVariables(char[] arr) {
		this(checkNonNull(arr, new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } }));
	}
	public IteratorWithVariables(short[] arr) {
		this(checkNonNull(arr,new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } }));
	}
	public IteratorWithVariables(int[] arr) {
		this(checkNonNull(arr,new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } }));
	}
	public IteratorWithVariables(long[] arr) {
		this(checkNonNull(arr, new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } }));
	}
	public IteratorWithVariables(float[] arr) {
		this(checkNonNull(arr,new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } }));
	}
	public IteratorWithVariables(double[] arr) {
		this(checkNonNull(arr,new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } }));
	}
	public IteratorWithVariables(Object[] arr) {
		this(checkNonNull(arr,new AbstractList() {
			public Object get(int index) { return arr[index]; }
			public int size() { return arr.length; } }));
	}
	/* @formatter:on */

	public IteratorWithVariables(Stream stream) {
		checkNonNull(stream, null);
		this.target = stream.iterator();
		index = -1;
	}

	public IteratorWithVariables(Iterable iterable) {
		checkNonNull(iterable, null);
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