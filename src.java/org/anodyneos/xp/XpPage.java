package org.anodyneos.xp;

import java.util.Properties;

import javax.servlet.jsp.el.ELException;

import org.xml.sax.SAXException;

public interface XpPage {

    public abstract void service(XpContext xpContext, XpContentHandler out)
    throws XpException, ELException, SAXException;
    public long getLoadTime();

    /**
     * XP output properties are defined by the optional <code>&lt;xp:output&gt;</code> tag.  See XpOutputKeys for
     * standard properties.
     *
     * @return the output <code>Properties</code> object.
     */
    public Properties getOutputProperties();
}
