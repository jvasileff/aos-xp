package org.anodyneos.xp.tagext;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author jvas
 *
 */
public abstract class XpFragment {

    public abstract XpContext getXpContext();
    public abstract void invoke(XpContentHandler xpCH) throws XpException, ELException, SAXException;
    public final String invokeToString() throws XpException, ELException, SAXException {
        StringBufferContentHandler sbCh = new StringBufferContentHandler();
        XpContentHandler xpCh = new XpContentHandler(sbCh);
        invoke(xpCh);
        return sbCh.getString();
    }

    private final class StringBufferContentHandler extends DefaultHandler {
        StringBuffer sb = new StringBuffer();

        public void characters(char[] chArray, int start, int length) throws SAXException {
            sb.append(chArray, start, length);
        }

        private String getString() {
            return sb.toString();
        }
    }
}
