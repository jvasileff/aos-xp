/*
 * Created on May 9, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.anodyneos.xp.http;

import org.anodyneos.xp.XpContext;


/**
 * @author jvas
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class HttpXpContext implements XpContext {

    public static final int PAGE_SCOPE = 0;
    public static final int REQUEST_SCOPE = 1;
    public static final int SESSION_SCOPE = 2;
    public static final int APPLICATION_SCOPE = 3;

    public static final String PAGE_SCOPE_STRING = "page";
    public static final String REQUEST_SCOPE_STRING = "request";
    public static final String SESSION_SCOPE_STRING = "session";
    public static final String APPLICATION_SCOPE_STRING = "application";

}
