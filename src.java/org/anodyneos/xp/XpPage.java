package org.anodyneos.xp;

import javax.servlet.jsp.el.ELException;

import org.xml.sax.SAXException;
import java.util.List;

public interface XpPage {

    public List getDependents();
    public abstract void service(XpContext xpContext, XpContentHandler out)
    throws XpException, ELException, SAXException;
    public long getLoadTime();
}
