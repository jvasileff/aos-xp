package org.anodyneos.xp.tagext;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.xml.sax.SAXException;

/**
 * @author jvas

 */
public interface XpTag {

    void doTag(XpOutput out) throws XpException, ELException, SAXException;
    XpTag getParent();
    void setXpBody(XpFragment xpBody);
    void setXpContext(XpContext xpc);
    void setParent(XpTag parent);

}
