package org.anodyneos.xp.tag.core;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.tagext.XpTagSupport;

/**
 * @author jvas
 */
public class NewBeanTag extends XpTagSupport {

    private String var;

    private String className;

    private String scope;

    public void doTag(XpContentHandler out) throws XpException, ELException {
        try {
            Object obj = Class.forName(className).newInstance();
            getXpContext().setAttribute(var, obj);
        } catch (ClassNotFoundException e) {
            throw new XpException(e);
        } catch (IllegalAccessException e) {
            throw new XpException(e);
        } catch (InstantiationException e) {
            throw new XpException(e);
        }
    }

    /**
     * @param className
     *            The className to set.
     */
    public void setClass(String className) {
        this.className = className;
    }

    /**
     * @param scope
     *            The scope to set.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * @param var
     *            The var to set.
     */
    public void setVar(String var) {
        this.var = var;
    }
}
