/*
 * Created on Jan 27, 2005
 */
package org.anodyneos.xp;

import java.util.Enumeration;

/**
 * @author jvas
 */
public interface XpNamespaceMapper  {

    String getPrefix(String uri);
    Enumeration getPrefixes();
    Enumeration getPrefixes(String uri);
    String getURI(String prefix);

}
