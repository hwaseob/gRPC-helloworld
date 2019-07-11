package com.uangel.svc.util;

import java.util.*;

public class TimedHashMap<K,V> implements Map<K,V> {

    class TimedEntry<T,E> implements Entry<T,E> {
        private static final long serialVersionUID = 5681390075518522107L;
        T key;
        E value;



        TimerTask timeoutTask = new TimerTask() {
            public void run() {
                synchronized ( TimedHashMap.this ) {
                    if ( containsKey(key) &&
                            baseMap.get( key )==TimedEntry.this) {
                        remove( key );
                    }
                }
            }
        };

        public TimedEntry(T key, E value) {
            super();
            this.key = key;
            this.value = value;
            tableTimer.schedule(timeoutTask, timeout);
        }

        public T getKey() {
            return key;
        }

        @Override
        public E setValue(E value) {
            E oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public E getValue() {
            return value;
        }
    }

    private long timeout;


    private static Timer tableTimer = new Timer(true);
    private Map<K, TimedEntry<K,V>> baseMap = new  LinkedHashMap<>();


    public TimedHashMap(long timeout) {
        super();
        this.timeout = timeout;
    }



    public void setTimeout(int timeout){
        this.timeout = timeout;
    }
    @Override
    public void clear() {
        baseMap.clear();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new HashSet<>(baseMap.values());
    }

    @Override
    public boolean containsKey(Object key) {
        return baseMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new IllegalStateException( "!!! NOT IMPLEMENTED !!!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get( Object key) {
        TimedEntry<K,V> e=baseMap.get((K) key);
        if (e==null)
            return null;
        return e.getValue();
    }



    @Override
    public boolean isEmpty() {
        return baseMap.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return baseMap.keySet();
    }

    @Override
    public V put(K key, V value) {
        V oldVal = null;
        if ( containsKey(key) ) {
            oldVal = get( key );
        }
        baseMap.put(key, new TimedEntry<K,V>( key, value) );
        return oldVal;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for ( Entry<? extends K, ? extends V> e : m.entrySet() ) {
            put( e.getKey(), e.getValue() );
        }
    }

    @Override
    public synchronized V remove(Object key) {
        TimedEntry<K,V> entry=baseMap.remove(key);
        if (entry != null)
            return entry.getValue();

        return null;
    }

    @Override
    public int size() {
        return baseMap.size();
    }

    @Override
    public Collection<V> values() {
        Set<V> valuesSet = new HashSet<>();
        for ( Entry<K,V> e : entrySet() ) {
            valuesSet.add(e.getValue());
        }
        return valuesSet;
    }

    /**
     * Returns a string representation of this map.  The string representation
     * consists of a list of key-value mappings in the order returned by the
     * map's <tt>entrySet</tt> view's iterator, enclosed in braces
     * (<tt>"{}"</tt>).  Adjacent mappings are separated by the characters
     * <tt>", "</tt> (comma and space).  Each key-value mapping is rendered as
     * the key followed by an equals sign (<tt>"="</tt>) followed by the
     * associated value.  Keys and values are converted to strings as by
     * {@link String#valueOf(Object)}.
     *
     * @return a string representation of this map
     */
    public String toString() {
        return baseMap.toString();
    }

}
