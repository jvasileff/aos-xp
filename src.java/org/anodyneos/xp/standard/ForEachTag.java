/*
 * Created on May 8, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.anodyneos.xp.standard;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpException;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;

/**
 * @author jvas
 *
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public final class ForEachTag extends XpTagSupport {

    private String var;
    private Object items;
    private String varStatus;
    private int begin = 0;
    private int end = 0;
    private int step = 1;

    private Object savedVar;
    private Object savedVarStatus;

    public ForEachTag() {
        super();
    }

    public void doTag() throws XpException, ELException, SAXException {
        if (begin > end || step < 1) {
            return;
        }
        saveVars();
        for (int i = begin; i <= end; i+=step) {
            if (null != var) {
                getXpContext().setAttribute(var, Integer.toString(i));
            }
            getXpBody().invoke();
        }
        restoreVars();
    }

    private void saveVars() {
        if (var != null) {
            savedVar = getXpContext().getAttribute(var);
        }
        if (varStatus != null) {
            savedVarStatus = getXpContext().getAttribute(varStatus);
        }
    }

    private void restoreVars() {
        if (var != null) {
            getXpContext().setAttribute(var, savedVar);
        }
        if (varStatus != null) {
            getXpContext().setAttribute(varStatus, savedVarStatus);
        }
    }

    /**
     * @param begin
     *            The begin to set.
     */
    public void setBegin(int begin) {
        this.begin = begin;
    }

    /**
     * @param end
     *            The end to set.
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * @param items
     *            The items to set.
     */
    public void setItems(Object items) {
        this.items = items;
    }

    /**
     * @param step
     *            The step to set.
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * @param var
     *            The var to set.
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * @param varStatus
     *            The varStatus to set.
     */
    public void setVarStatus(String varStatus) {
        this.varStatus = varStatus;
    }
}
