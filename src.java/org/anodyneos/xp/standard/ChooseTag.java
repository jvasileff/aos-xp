/*
 * Created on May 9, 2004
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
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChooseTag extends XpTagSupport {

    private boolean complete = false;

    /**
     *
     */
    public ChooseTag() {
        super();
    }

    public void doTag() throws XpException, ELException, SAXException {
        getXpBody().invoke();
    }

    boolean isComplete() {
        return complete;
    }

    void markComplete() {
        complete=true;
    }

}
