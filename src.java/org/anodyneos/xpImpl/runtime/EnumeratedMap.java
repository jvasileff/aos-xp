package org.anodyneos.xpImpl.runtime;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class EnumeratedMap<K, V> implements Map<K, V> {

    public abstract Enumeration<K> enumerateKeys();
    public abstract boolean isMutable ();
    public abstract V getValue (Object key);

    private Map<K,V> cachedMap;

    private Map<K,V> getAsMap() {
        Map<K,V> m;
        if (cachedMap != null) {
            m = cachedMap;
        } else {
            m =  new HashMap<K,V>();
            for (Enumeration<K> e = enumerateKeys(); e.hasMoreElements(); ) {
                K key = e.nextElement();
                V value = getValue(key);
                m.put(key, value);
            }
            if (!isMutable()) {
                cachedMap = m;
            }
        }
        return m;
    }

    public int size() {
        if (isMutable()) {
            return getAsMap().size();
        } else {
            int i = 0;
            for (Enumeration<K> e = enumerateKeys(); e.hasMoreElements(); e.nextElement()) {
                i++;
            }
            return i;
        }
    }

    public boolean isEmpty() {
        return !enumerateKeys().hasMoreElements();
    }

    public boolean containsKey(Object key) {
        return getValue(key) != null;
    }

    public boolean containsValue(Object value) {
        return getAsMap().containsValue(value);
    }

    public V get(Object key) {
        return getValue(key);
    }

    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        for(K key : t.keySet()) {
            put(key, t.get(key));
        }
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Set<K> keySet() {
        return getAsMap().keySet();
    }

    public Collection<V> values() {
        return getAsMap().values();
    }

    public Set<Entry<K, V>> entrySet() {
        return getAsMap().entrySet();
    }

}
