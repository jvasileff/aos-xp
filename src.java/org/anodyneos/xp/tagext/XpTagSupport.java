package org.anodyneos.xp.tagext;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.xml.sax.SAXException;


/**
 * @author jvas
 *
 */
public abstract class XpTagSupport implements XpTag {
    private XpTag parent;

    private XpFragment xpBody;
    private XpContext xpContext;

    public XpTagSupport() {
        super();
    }

    /*
    public void doTag(XpContentHandler out) throws XpException, ELException, SAXException {
        // by default, do nothing;
    }
    */
    public abstract void doTag(XpContentHandler out) throws XpException, ELException, SAXException;

    public static final XpTag findAncestorWithClass(XpTag fromTag, Class theClass) {
        if (null == fromTag || null == theClass) {
            return null;
        } else {
            boolean isInterface = theClass.isInterface();
            Class fromTagClass = fromTag.getClass();
            fromTag = fromTag.getParent();
            while(  fromTag != null &&
                    (isInterface && ! theClass.isInstance(fromTag)) ||
                    ! theClass.isAssignableFrom(fromTagClass)) {
                fromTag = fromTag.getParent();
            }
            return fromTag;
        }
    }

    public final XpTag getParent() {
        return parent;
    }

    public final XpFragment getXpBody() {
        return xpBody;
    }

    public final XpContext getXpContext() {
        return xpContext;
    }

    public final void setParent(XpTag parent) {
        this.parent = parent;
    }

    public final void setXpBody(XpFragment xpBody) {
        this.xpBody = xpBody;
    }

    public final void setXpContext(XpContext xpc) {
        this.xpContext = xpc;
    }

}
