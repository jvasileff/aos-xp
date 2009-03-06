package org.anodyneos.xp.standalone;

import java.util.Enumeration;

public interface StandaloneXpAppContext {

    Object getAttribute(String name);
    void removeAttribute(String name);
    void setAttribute(String name, Object obj);
    Enumeration getAttributeNames();

}
