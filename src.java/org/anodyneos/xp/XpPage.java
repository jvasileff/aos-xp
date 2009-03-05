package org.anodyneos.xp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

import javax.servlet.jsp.el.ELException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.anodyneos.commons.xml.xsl.TemplatesCache;
import org.xml.sax.SAXException;

public interface XpPage {

    static final String METHOD_HTML = "html";
    static final String METHOD_XHTML = "xhtml";
    static final String METHOD_XHTML_AUTO = "xhtmlAuto";
    static final String MEDIA_TYPE_XHTML = "application/xhtml+xml";
    static final String MEDIA_TYPE_HTML = "text/html";

    void service(XpContext xpContext, XpOutput out) throws XpException, ELException, SAXException;

    /**
     * XP output properties are defined by the optional <code>&lt;xp:output&gt;</code> tag.  See XpOutputKeys for
     * standard properties.
     *
     * @return the output <code>Properties</code> object.
     */
    Properties getOutputProperties();

    URI getSourceURI();

    void run(XpContext xpContext, OutputStream out) throws TransformerConfigurationException,
                TransformerException, IOException;

    String getEncoding();
    void setEncoding(String encoding);

    String getIndent();
    void setIndent(String indent);

    String getIndentAmount();
    void setIndentAmount(String indentAmount);

    String getOmitXmlDeclaration();
    void setOmitXmlDeclaration(String omitXmlDeclaration);

    String getMediaType();
    void setMediaType(String mediaType);

    String getMethod();
    void setMethod(String method);

    String getCdataSectionElements();
    void setCdataSectionElements(String cdataSectionElements);

    String getDoctypePublic();
    void setDoctypePublic(String doctypePublic);

    String getDoctypeSystem();
    void setDoctypeSystem(String doctypeSystem);

    TemplatesCache getTemplatesCache();
    void setTemplatesCache(TemplatesCache templatesCache);

    void setUserAgent(String userAgent);

}
