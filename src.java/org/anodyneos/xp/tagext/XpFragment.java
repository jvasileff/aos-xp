package org.anodyneos.xp.tagext;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.util.TextContentHandler;
import org.xml.sax.SAXException;

/**
 * @author jvas
 *
 */
public abstract class XpFragment {

    public abstract XpContext getXpContext();
    public abstract void invoke(XpOutput out) throws XpException, ELException, SAXException;
    public final String invokeToString() throws XpException, ELException, SAXException {
        TextContentHandler sbCh = new TextContentHandler();
        XpOutput newOut = new XpOutput(sbCh);
        invoke(newOut);
        return sbCh.getText();
    }

}
