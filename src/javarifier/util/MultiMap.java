package javarifier.util;

import java.util.Set;

public interface MultiMap<K, V> {

	public void putAll(MultiMap<K, V> m);

	public boolean isEmpty();

	public int numKeys();

	public boolean containsKey(Object key);

	public boolean containsValue(Object value);

	public boolean put(K key, V value);

	public boolean putAll(K key, Set<V> values);

	public boolean remove(K key, V value);

	/** Removes the key key and the set pointed to by key */
	public boolean remove(Object key);

	/** Removes values from the set pointed to by key */
	public boolean removeAll(K key, Set<V> values);

        /** Removes all keys in keys and all corrisponding sets */
        public boolean removeAll(Set<K> keys);

	public Set<V> get(Object o);

	public Set<K> keySet();

	public Set<V> values();

	public boolean equals(Object o);

	public int hashCode();

}
