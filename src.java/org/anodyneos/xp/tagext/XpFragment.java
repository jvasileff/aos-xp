package org.anodyneos.xp.tagext;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.xml.sax.SAXException;


/**
 * @author jvas
 *
 */
public interface XpFragment {

    XpContext getXpContext();
    void invoke() throws XpException, ELException, SAXException;

}
