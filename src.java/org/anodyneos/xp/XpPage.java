package org.anodyneos.xp;

import javax.servlet.jsp.el.ELException;

import org.xml.sax.SAXException;
import java.util.List;
import java.util.Properties;

public interface XpPage {

    public List getDependents();
    public abstract void service(XpContext xpContext, XpContentHandler out)
    throws XpException, ELException, SAXException;
    public long getLoadTime();

    /**
     * XP output properties are defined by the optional <code>&lt;xp:output&gt;</code> tag.  They include:
     *
     *  <pre>
     *  cdataSectionElements="xxx"
     *  doctypePublic
     *  doctypeSystem
     *  encoding
     *  indent="yes|no"
     *  mediaType
     *  method="xml|html"
     *  omitXmlDeclaration="yes|no"
     *  xhtmlCompat="yes|no"
     *  xsltURI
     *  </pre>
     *
     * @return the output <code>Properties</code> object.
     */
    public Properties getOutputProperties();
}
