package org.anodyneos.xp.standalone;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class StandaloneXpAppContextMapAdapter implements StandaloneXpAppContext {

    private Map backingMap;

    public StandaloneXpAppContextMapAdapter() {
        this.backingMap = Collections.synchronizedMap(new HashMap());
    }

    /**
     * @param map Must be thread safe (ie Collections.synchronizedMap(map))
     */
    public StandaloneXpAppContextMapAdapter(Map map) {
        this.backingMap = map;
    }

    /**
     * @param map Must be thread safe (ie Collections.synchronizedMap(map))
     */
    public void setBackingMap(Map map) {
        this.backingMap = map;
    }

    public Object getAttribute(String name) {
        return backingMap.get(name);
    }

    public void removeAttribute(String name) {
        backingMap.remove(name);
    }

    public void setAttribute(String name, Object obj) {
        backingMap.put(name, obj);
    }

    public Enumeration getAttributeNames() {
        Set keys = backingMap.keySet();
        final String[] array = (String[]) keys.toArray(new String[keys.size()]);
        return new Enumeration() {
            private int next = 0;

            public boolean hasMoreElements() {
                if(next >= array.length) {
                    return false;
                } else {
                    return true;
                }
            }

            public Object nextElement() throws NoSuchElementException {
                if(! hasMoreElements()) {
                    throw new NoSuchElementException("no more elements.");
                }
                return array[next++];
            }
        };
    }

}
