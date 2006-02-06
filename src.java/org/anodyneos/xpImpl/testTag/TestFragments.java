package org.anodyneos.xpImpl.testTag;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.tagext.XpFragment;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;


/**
 * @author jvas
 */
public class TestFragments extends XpTagSupport {

    XpFragment frag1;
    XpFragment frag2;

    public TestFragments() {
        super();
    }

    public void doTag(XpOutput out) throws XpException, ELException, SAXException {
        frag1.invoke(out);
        frag2.invoke(out);
    }

    public void setFrag1(XpFragment frag1) {
        this.frag1= frag1;
    }

    public void setFrag2(XpFragment frag2) {
        this.frag2= frag2;
    }
}
