/*
 * Created on May 9, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.anodyneos.xp.http;

import javax.servlet.http.HttpServlet;

import org.anodyneos.xp.XpPage;


/**
 * Is this a good idea? If we separated XP from the servlet API we could
 * achieve more flexibility. The XP engine's XP servlet could allow XPs to work
 * like JSPs in a webapp, and, the XP API could support direct access to XP's
 * to use them like javax.transform, ie, get an instance, customize it, and
 * call it directly with an outputsource from another servlet.
 *
 * On advantage may be the ability to forward to a servlet (without the servlet
 * class being mapped) from another servlet... is this possible with the
 * servlet spec?
 *
 * @author jvas
 */
public abstract class HttpXpPage extends HttpServlet implements XpPage {

    public abstract void _xpService(HttpXpContext xpContext) throws org.xml.sax.SAXException;

}
