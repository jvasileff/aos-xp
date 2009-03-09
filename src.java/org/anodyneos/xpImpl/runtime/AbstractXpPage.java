package org.anodyneos.xpImpl.runtime;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Properties;

import javax.servlet.jsp.el.ELException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.anodyneos.commons.xml.StripNamespaceFilter;
import org.anodyneos.commons.xml.xsl.TemplatesCache;
import org.anodyneos.servlet.util.BrowserDetector;
import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.XpOutputKeys;
import org.anodyneos.xp.XpPage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public abstract class AbstractXpPage implements XpPage {

    private static final Log log = LogFactory.getLog(AbstractXpPage.class);

    private static final long serialVersionUID = 1L;

    public static final String METHOD_HTML = "html";
    public static final String METHOD_XHTML = "xhtml";
    public static final String METHOD_XHTML_AUTO = "xhtmlAuto";
    public static final String MEDIA_TYPE_XHTML = "application/xhtml+xml";
    public static final String MEDIA_TYPE_HTML = "text/html";

    public static final String KEY_XALAN_INDENT_AMOUNT = "{http://xml.apache.org/xalan}indent-amount";

    private TemplatesCache templatesCache;
    private String encoding = "UTF-8";
    private String indent = "no";
    private String indentAmount = "1";
    private String omitXmlDeclaration = "no";
    private String mediaType = "text/xml";
    private String method = "xml";
    private String cdataSectionElements = "";
    private String doctypePublic = "";
    private String doctypeSystem = "";

    protected abstract Properties getOutputProperties();
    protected abstract void service(XpContext xpContext, XpOutput out) throws XpException, ELException, SAXException;

    public void service(XpContext xpContext, OutputStream out) throws IOException, XpException {

        String xsltURI = getOutputProperties().getProperty(XpOutputKeys.XSLT_URI);

        boolean isIdentityTransformer = false;
        Transformer trans = newTransformer(xsltURI);
        if (null == trans) {
            isIdentityTransformer = true;
            trans = newTransformer();
        }

        XMLReader xpXmlReader = new XpPageReader(this, xpContext);

        try {
            if ("fop".equals(method)) {
                resetProperties(trans);
                setTransformerProp(trans, OutputKeys.METHOD, "xml");
                FopOutputer.outputFop(xpXmlReader, trans, out);
                out.flush();
            } else if (METHOD_HTML.equals(method)) {
                XMLFilterImpl nsFilter = new StripNamespaceFilter();
                if (isIdentityTransformer) {
                    // xp -> nsfilter -> identityXSL Template
                    resetProperties(trans);
                    nsFilter.setParent(xpXmlReader);
                    setTransformerProp(trans, OutputKeys.METHOD, "html");
                    setTransformerProp(trans, OutputKeys.MEDIA_TYPE, mediaType);
                    Source source = new SAXSource(nsFilter, new InputSource(""));
                    trans.transform(source, new StreamResult(out));
                    out.flush();
                } else {
                    // xp -> XSL Template -> nsfilter -> identityXSL TH
                    TransformerHandler th = templatesCache.getTransformerHandler();
                    resetProperties(th.getTransformer());
                    setTransformerProp(th.getTransformer(), OutputKeys.METHOD, "html");
                    setTransformerProp(th.getTransformer(), OutputKeys.MEDIA_TYPE, mediaType);
                    setTransformerProp(trans, OutputKeys.METHOD, "xml");

                    // xp source outputs to transformer
                    Source source = new SAXSource(xpXmlReader, new InputSource(""));
                    // transformer outputs to nsFilter (acting as a contentHandler)
                    SAXResult transformerSaxResult = new SAXResult(nsFilter);
                    // nsFilter (acting as an XMLReader) outputs to th
                    nsFilter.setContentHandler(th);
                    // th outputs to browser
                    th.setResult(new StreamResult(out));

                    // do it
                    trans.transform(source, transformerSaxResult);
                    out.flush();
                }
            } else if ("text".equals(method)) {
                // same as default, except let xalan think utf-8, and handle our own text encoding
                // OutputStreamWriter uses "?" for unavailable characters, while xalan likes to complain
                // to stderr for each one, and then follow up with &#nnnn; which doesn't make sense for text.
                resetProperties(trans);
                setTransformerProp(trans, OutputKeys.ENCODING, "UTF-8");
                setTransformerProp(trans, OutputKeys.METHOD, method);
                setTransformerProp(trans, OutputKeys.MEDIA_TYPE, mediaType);
                Writer writer = new BufferedWriter(new OutputStreamWriter(out, getEncoding()));
                Source source = new SAXSource(xpXmlReader, new InputSource(""));
                trans.transform(source, new StreamResult(writer));
                writer.flush();
            } else {
                resetProperties(trans);
                setTransformerProp(trans, OutputKeys.METHOD, method);
                setTransformerProp(trans, OutputKeys.MEDIA_TYPE, mediaType);
                Source source = new SAXSource(xpXmlReader, new InputSource(""));
                trans.transform(source, new StreamResult(out));
                out.flush();
            }
        } catch (TransformerException e) {
            throw new XpException(e);
        }
    }

    void init() throws XpException {
        updatePropertiesFromXpPage();
    }

    /*
    private void updatePropertiesFromTransformer() {
        cdataSectionElements = propWithDefault(transformer, OutputKeys.CDATA_SECTION_ELEMENTS, cdataSectionElements);
        doctypePublic = propWithDefault(transformer, OutputKeys.DOCTYPE_PUBLIC, doctypePublic);
        doctypeSystem = propWithDefault(transformer, OutputKeys.DOCTYPE_SYSTEM, doctypeSystem);
        encoding = propWithDefault(transformer, OutputKeys.ENCODING, encoding);
        indent = propWithDefault(transformer, OutputKeys.INDENT, indent);
        indentAmount = propWithDefault(transformer, KEY_XALAN_INDENT_AMOUNT, indentAmount);
        mediaType = propWithDefault(transformer, OutputKeys.MEDIA_TYPE, mediaType);
        method = propWithDefault(transformer, OutputKeys.METHOD, method);
        omitXmlDeclaration = propWithDefault(transformer, OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration);
        //standalone = propWithDefault(trans, OutputKeys.STANDALONE, doctypePublic);
    }

    private String propWithDefault(Transformer transformer, String key, String defaultValue) {
        String val = transformer.getOutputProperty(key);
        if (null != val && val.length() > 0) {
            return val;
        } else {
            return defaultValue;
        }
    }
    */

    private void updatePropertiesFromXpPage() {
        Properties props = getOutputProperties();
        cdataSectionElements = props.getProperty(XpOutputKeys.CDATA_SECTION_ELEMENTS, cdataSectionElements);
        doctypePublic = props.getProperty(XpOutputKeys.DOCTYPE_PUBLIC, doctypePublic);
        doctypeSystem = props.getProperty(XpOutputKeys.DOCTYPE_SYSTEM, doctypeSystem);
        encoding = props.getProperty(XpOutputKeys.ENCODING, encoding);
        indent = props.getProperty(XpOutputKeys.INDENT, indent);
        indentAmount = props.getProperty(XpOutputKeys.INDENT_AMOUNT, indentAmount);
        mediaType = props.getProperty(XpOutputKeys.MEDIA_TYPE, mediaType);
        method = props.getProperty(XpOutputKeys.METHOD, method);
        omitXmlDeclaration = props.getProperty(XpOutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration);

        if (METHOD_XHTML_AUTO.equals(method)) {
            method = METHOD_HTML;
            mediaType = MEDIA_TYPE_HTML;
        }
    }

    private void resetProperties(Transformer trans) {
        trans.clearParameters();
        setTransformerProp(trans, OutputKeys.CDATA_SECTION_ELEMENTS, cdataSectionElements);
        setTransformerProp(trans, OutputKeys.DOCTYPE_PUBLIC, doctypePublic);
        setTransformerProp(trans, OutputKeys.DOCTYPE_SYSTEM, doctypeSystem);
        setTransformerProp(trans, OutputKeys.ENCODING, encoding);
        setTransformerProp(trans, OutputKeys.INDENT, indent);
        setTransformerProp(trans, KEY_XALAN_INDENT_AMOUNT, indentAmount);
        setTransformerProp(trans, OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration);
    }

    private Transformer newTransformer(String xsltURI) throws XpException {
        Transformer transformer;

        // get the transformer if specified
        if (null == xsltURI || xsltURI.length() == 0) {
            transformer = null;
        } else {
            try {
                URI resolvedURI = getSourceURI().resolve(xsltURI);
                if(log.isDebugEnabled()) {
                    log.debug("Using xslURI: " + resolvedURI);
                }
                transformer = templatesCache.getTransformer(resolvedURI);
            } catch(FileNotFoundException fnf) {
                throw new XpException("Unable to load " + getClass().getCanonicalName() + ".xp " +
                    "Check the xsltURI attribute of your xp file.  FileNotFound: " + fnf.getMessage());
            } catch(TransformerConfigurationException ex) {
                throw new XpException("Unable to load " + getClass().getCanonicalName() + ".xp " +
                    "Check the xsltURI attribute of your xp file.  TransformerConfigurationException: " + ex.getMessage());
            } catch(IOException ex) {
                throw new XpException("Unable to load " + getClass().getCanonicalName() + ".xp " +
                    "Check the xsltURI attribute of your xp file.  IOException: " + ex.getMessage());
            }
        }

        return transformer;
    }

    private Transformer newTransformer() throws XpException {
        try {
            return getTemplatesCache().getTransformer();
        } catch (TransformerConfigurationException e) {
            throw new XpException(e);
        }
    }

    private void setTransformerProp(Transformer trans, String key, String value) {
        if(null != value && value.length() > 0) {
            trans.setOutputProperty(key, value);
        }
    }

    public String getEncoding() { return encoding; }
    public void setEncoding(String encoding) { this.encoding = encoding; }

    public String getIndent() { return indent; }
    public void setIndent(String indent) { this.indent = indent; }

    public String getIndentAmount() { return indentAmount; }
    public void setIndentAmount(String indentAmount) { this.indentAmount = indentAmount; }

    public String getOmitXmlDeclaration() { return omitXmlDeclaration; }
    public void setOmitXmlDeclaration(String omitXmlDeclaration) { this.omitXmlDeclaration = omitXmlDeclaration; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getMethod() { return method; }
    public void setMethod(String method) {
        if (METHOD_XHTML_AUTO.equals(method)) {
            method = METHOD_HTML;
            mediaType = MEDIA_TYPE_HTML;
        } else {
            this.method = method;
        }
    }

    public String getCdataSectionElements() { return cdataSectionElements; }
    public void setCdataSectionElements(String cdataSectionElements) { this.cdataSectionElements = cdataSectionElements; }

    public String getDoctypePublic() { return doctypePublic; }
    public void setDoctypePublic(String doctypePublic) { this.doctypePublic = doctypePublic; }

    public String getDoctypeSystem() { return doctypeSystem; }
    public void setDoctypeSystem(String doctypeSystem) { this.doctypeSystem = doctypeSystem; }

    public TemplatesCache getTemplatesCache() { return templatesCache; }
    public void setTemplatesCache(TemplatesCache templatesCache) { this.templatesCache = templatesCache; }

    public void configureForUserAgent(String userAgent) {
        if (METHOD_XHTML_AUTO.equals(getOutputProperties().getProperty(XpOutputKeys.METHOD))) {
            BrowserDetector bd = getBrowserDetector(userAgent);
            if ((BrowserDetector.MOZILLA.equals(bd.getBrowserName()) && bd.getBrowserVersion() >= 5)
                    || BrowserDetector.SAFARI.equals(bd.getBrowserName())) {
                method = METHOD_XHTML;
                mediaType = MEDIA_TYPE_XHTML;
            } else {
                method = METHOD_HTML;
                mediaType = MEDIA_TYPE_HTML;
            }
            if (log.isDebugEnabled()) {
                log.debug(
                      "User-Agent: " + userAgent
                    + ";Browser name: " + bd.getBrowserName() + ";version: " + bd.getBrowserVersion()
                    + ";doXhtmlToHtml: true");
            }
        }
    }

    private BrowserDetector getBrowserDetector(String userAgent) {
        BrowserDetector browserDetector;
        if (userAgent == null) {
            userAgent = "";
        }
        browserDetector = new BrowserDetector(userAgent);
        return browserDetector;
    }


}
