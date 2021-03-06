package org.anodyneos.xpImpl.runtime;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;

public class XpPageReader implements XMLReader {

    private static final Log logger = LogFactory.getLog(XpPageReader.class);

    private AbstractXpPage xp;
    private XpContext xpContext;

    private static final String FEATURE_NAMESPACES = "http://xml.org/sax/features/namespaces";

    private static final String FEATURE_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";

    /**
     * current setting for the SAX feature
     * "http://xml.org/sax/features/namespace-prefixes".
     */
    private boolean namespacePrefixes = false;

    public XpPageReader(AbstractXpPage xp, XpContext xpContext) {
        this.xp = xp;
        this.xpContext = xpContext;
    }

    public void parse(InputSource input) throws SAXException {

        XpOutput out = new XpOutput(getContentHandler(), namespacePrefixes);

        // process
        getContentHandler().startDocument();
        try {
            xp.service(xpContext, out);
        } catch (XpException e) {
            throw new SAXException(e);
        } catch (ELException e) {
            throw new SAXException(e);
        }
        getContentHandler().endDocument();
    }

    public void parse(String systemId) {
        throw new java.lang.UnsupportedOperationException();
    }

    // generic stuff
    protected ContentHandler contentHandler;

    protected DTDHandler dtdHandler;

    protected EntityResolver entityResolver;

    protected ErrorHandler errorHandler;

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public boolean getFeature(java.lang.String name) throws SAXNotRecognizedException {
        if (FEATURE_NAMESPACE_PREFIXES.equals(name)) {
            return this.namespacePrefixes;
        } else if (FEATURE_NAMESPACES.equals(name)) {
            return true;
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("Feature " + name + " not recognized.");
            }
            throw new SAXNotRecognizedException("Feature " + name + " not recognized.");
        }
    }

    public java.lang.Object getProperty(java.lang.String name) {
        return null;
    }

    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    public void setDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    public void setFeature(java.lang.String name, boolean value) throws SAXNotRecognizedException {
        if (FEATURE_NAMESPACE_PREFIXES.equals(name)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Setting feature " + name + "=" + value + ".");
            }
            this.namespacePrefixes = value;
        } else if (FEATURE_NAMESPACES.equals(name)) {
            if (!value) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Turning off feature " + name + " not supported.");
                }
            }
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("Feature " + name + "=" + value + " not recognized.");
            }
            throw new SAXNotRecognizedException("Feature " + name + "=" + value + " not recognized.");
        }
    }

    public void setProperty(java.lang.String name, java.lang.Object value) {
    }

}
