package org.anodyneos.xp;

import javax.servlet.jsp.el.ELException;

import org.xml.sax.SAXException;

public interface XpPage {

    public abstract void service(XpContext xpContext, XpContentHandler out)
    throws XpException, ELException, SAXException;

}
