package ngoy.core;

import java.util.Map;

/**
 * A pair.
 * 
 * @author krizz
 * @param <K> Type of key
 * @param <V> Type of value
 */
public final class Pair<K, V> implements Map.Entry<K, V> {
	/**
	 * Constructs a new pair.
	 * 
	 * @param key
	 * @param value
	 * @return pair
	 */
	public static <K, V> Pair<K, V> pair(K key, V value) {
		return new Pair<K, V>(key, value);
	}

	private final K key;
	private final V value;

	private Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		throw new UnsupportedOperationException();
	}
}