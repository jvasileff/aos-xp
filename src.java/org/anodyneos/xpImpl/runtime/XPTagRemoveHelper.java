package org.anodyneos.xpImpl.runtime;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;


/**
 * @author jvas
 */
public class XPTagRemoveHelper {

    private XPTagRemoveHelper() {
        super();
    }

    public static void remove(XpContext xpContext, String var, String scope) throws XpException, ELException {

        if (null == scope) {
            xpContext.removeAttribute(var);
        } else {
            xpContext.removeAttribute(var, xpContext.resolveScope(scope));
        }
    }

}
