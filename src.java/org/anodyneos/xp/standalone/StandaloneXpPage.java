package org.anodyneos.xp.standalone;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpPage;
import org.xml.sax.SAXException;

/**
 * @author jvas
 *
 */
public abstract class StandaloneXpPage implements XpPage {

    public abstract void _xpService(StandaloneXpContext xpContext, XpContentHandler out)
    throws XpException, ELException, SAXException;

}
