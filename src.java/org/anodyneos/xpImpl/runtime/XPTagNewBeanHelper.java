package org.anodyneos.xpImpl.runtime;

import java.beans.Beans;
import java.io.IOException;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;

/**
 * @author jvas
 */
public class XPTagNewBeanHelper {

    private XPTagNewBeanHelper() {
        super();
    }

    public static void newBean(XpContext xpContext, String var, String scope, String className) throws XpException, ELException {

        try {
            Object obj = Beans.instantiate(Thread.currentThread().getContextClassLoader(), className);
            xpContext.setAttribute(var, obj);
        } catch (ClassNotFoundException e) {
            throw new XpException(e);
        } catch (IOException e) {
            throw new XpException(e);
        }
    }

}
