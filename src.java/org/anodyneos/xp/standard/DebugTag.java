/*
 * Created on Jul 7, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.anodyneos.xp.standard;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;



/**
 * @author jvas
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DebugTag extends XpTagSupport {

    /**
     *
     */
    public DebugTag() {
        super();
    }

    /* (non-Javadoc)
     * @see org.anodyneos.xp.tagext.XpTag#doTag(org.anodyneos.xp.XpContentHandler)
     */
    public void doTag(XpContentHandler out) throws XpException, ELException, SAXException {
        XpContentHandler newCH = new XpContentHandler(new DebugCH(System.err, out));
        getXpBody().invoke(newCH);
    }

}
