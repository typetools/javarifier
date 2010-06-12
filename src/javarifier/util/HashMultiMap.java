package javarifier.util;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/** A map with sets as values, HashMap implementation.
 *
 * @author Ondrej Lhotak
 */

public class HashMultiMap<K, V> implements MultiMap<K, V>  {
        Map<K, Set<V>> m = new HashMap<K, Set<V>>(0);

        public HashMultiMap() {}
        public HashMultiMap( MultiMap<K, V> m ) {
                putAll( m );
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#putAll(javarifier.MultiMap)
         */
        public void putAll( MultiMap<K, V> m ) {
                Iterator<K> it = m.keySet().iterator();
                while( it.hasNext() ) {
                        K o = it.next();
                        putAll( o, m.get( o ) );
                }
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#isEmpty()
         */
        public boolean isEmpty() {
                return numKeys() == 0;
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#numKeys()
         */
        public int numKeys() {
                return m.size();
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#containsKey(java.lang.Object)
         */
        public boolean containsKey( Object key ) {
                return m.containsKey( key );
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#containsValue(java.lang.Object)
         */
        public boolean containsValue( Object value ) {
                Iterator<Set<V>> it = m.values().iterator();
                while( it.hasNext() ) {
                        Set<V> s = it.next();
                        if ( s.contains( value ) ) return true;
                }
                return false;
        }
        protected Set<V> newSet() {
                return new LinkedHashSet<V>(4);
        }
        private Set<V> findSet( K key ) {
                Set<V> s = m.get( key );
                if ( s == null ) {
                        s = newSet();
                        m.put( key, s );
                }
                return s;
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#put(K, V)
         */
        public boolean put( K key, V value ) {
                return findSet( key ).add( value );
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#putAll(K, java.util.Set)
         */
        public boolean putAll( K key, Set<V> values ) {
                if (values.isEmpty()) return false;
                return findSet( key ).addAll( values );
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#remove(K, V)
         */
        public boolean remove( K key, V value ) {
                Set<V> s =  m.get( key );
                if ( s == null ) return false;
                boolean ret = s.remove( value );
                if ( s.isEmpty() ) {
                        m.remove( key );
                }
                return ret;
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#remove(java.lang.Object)
         */
        public boolean remove( Object key ) {
                return null != m.remove( key );
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#removeAll(K, java.util.Set)
         */
        public boolean removeAll( K key, Set<V> values ) {
                Set<V> s = m.get( key );
                if ( s == null ) return false;
                boolean ret = s.removeAll( values );
                if ( s.isEmpty() ) {
                        m.remove( key );
                }
                return ret;
        }

        public boolean removeAll(Set<K> keys) {
                boolean ret = false;
                for (K key : keys) {
                        ret = ret || remove(key); // note side-effects
                }
                return ret;
        }

        /* (non-Javadoc)
         * @see javarifier.MultiMap#get(K)
         */
        public Set<V> get( Object o ) {
                Set<V> ret = m.get( o );
                if ( ret == null ) return Collections.emptySet();
                return Collections.unmodifiableSet(ret);
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#keySet()
         */
        public Set<K> keySet() {
                return m.keySet();
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#values()
         */
        public Set<V> values() {
                Set<V> ret = new LinkedHashSet<V>(0);
                Iterator<Set<V>> it = m.values().iterator();
                while( it.hasNext() ) {
                        Set<V> s = it.next();
                        ret.addAll( s );
                }
                return ret;
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#equals(java.lang.Object)
         */
        public boolean equals( Object o ) {
                if ( ! (o instanceof HashMultiMap) ) return false;
                MultiMap<?, ?> mm = (MultiMap<?, ?>) o;
                if ( !keySet().equals( mm.keySet() ) ) return false;
                Iterator<Map.Entry<K, Set<V>>> it = m.entrySet().iterator();
                while( it.hasNext() ) {
                        Map.Entry<K, Set<V>> e = it.next();
                        Set<V> s = e.getValue();
                        if ( !s.equals( mm.get( e.getKey() ) ) ) return false;
                }
                return true;
        }
        /* (non-Javadoc)
         * @see javarifier.MultiMap#hashCode()
         */
        public int hashCode() {
                return m.hashCode();
        }

        public String toString() {
                return m.toString();
        }
}
