/*
 * Created on May 9, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.anodyneos.xp.standard;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;


/**
 * @author jvas
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class IfTag extends XpTagSupport {

    private boolean test = false;
    private String var;
    private String scope;

    /**
     *
     */
    public IfTag() {
        super();
    }

    public void doTag(XpContentHandler out) throws XpException, ELException, SAXException {
        if(var != null && scope != null) {
            getXpContext().setAttribute(var, Boolean.valueOf(test), getXpContext().resolveScope(scope));
        } else if (var!=null) {
            getXpContext().setAttribute(var, Boolean.valueOf(test));
        }
        if(test) {
            getXpBody().invoke(out);
        }
    }

    /**
     * @param scope The scope to set.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }
    /**
     * @param test The test to set.
     */
    public void setTest(boolean test) {
        this.test = test;
    }
    /**
     * @param var The var to set.
     */
    public void setVar(String var) {
        this.var = var;
    }
}
