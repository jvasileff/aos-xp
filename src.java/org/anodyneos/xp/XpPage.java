package org.anodyneos.xp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.anodyneos.commons.xml.xsl.TemplatesCache;

public interface XpPage {

    static final String METHOD_HTML = "html";
    static final String METHOD_XHTML = "xhtml";
    static final String METHOD_XHTML_AUTO = "xhtmlAuto";
    static final String MEDIA_TYPE_XHTML = "application/xhtml+xml";
    static final String MEDIA_TYPE_HTML = "text/html";

    void service(XpContext xpContext, OutputStream out) throws IOException, XpException;

    /**
     * XP output properties are defined by the optional <code>&lt;xp:output&gt;</code> tag.  See XpOutputKeys for
     * standard properties.
     *
     * @return the output <code>Properties</code> object.
     */

    URI getSourceURI();

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

    void configureForUserAgent(String userAgent);

}
