package org.anodyneos.xpImpl.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.net.URI;
import org.anodyneos.commons.xml.StripNamespaceFilter;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.commons.xml.xsl.TemplatesCache;
import org.anodyneos.servlet.net.ServletContextURIHandler;
import org.anodyneos.servlet.util.BrowserDetector;
import org.anodyneos.xp.XpCompilationException;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpFileNotFoundException;
import org.anodyneos.xp.XpOutputKeys;
import org.anodyneos.xp.XpPage;
import org.anodyneos.xp.XpTranslationException;
import org.anodyneos.xp.XpXMLReader;
import org.anodyneos.xp.http.HttpXpContext;
import org.anodyneos.xpImpl.http.HttpXpContextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.Driver;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class XpServlet extends HttpServlet{

    protected final Log logger = LogFactory.getLog(getClass());

    private static final long serialVersionUID = 3258132440416794419L;

    public static final String TMP_DIR = "javax.servlet.context.tempdir";

    private static final String XP_REGISTRY="xpRegistry";
    private static final String XP_CACHE_AUTOLOAD="xpCacheAutoload";
    private TransformerFactory tf = TransformerFactory.newInstance();
    private XpCachingLoader cache = XpCachingLoader.getLoader();

    private TemplatesCache templatesCache;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        File scratchDir = (File) getServletContext().getAttribute(TMP_DIR);
        if (!(scratchDir.exists() && scratchDir.canRead() && scratchDir.canWrite())){
            throw new ServletException("Work directory is invalid.  Check for existance and Read/Write settings.");
        }
        String scratchDirPath = scratchDir.getAbsolutePath();

        UnifiedResolver resolver = new UnifiedResolver();
        resolver.setDefaultLookupEnabled(false);
        resolver.addProtocolHandler("classpath",
                new ClassLoaderURIHandler(Thread.currentThread().getContextClassLoader()));
        resolver.addProtocolHandler("webapp",
                new ServletContextURIHandler(servletConfig.getServletContext()));

        URLClassLoader urlLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();

        if (urlLoader == null) {
            urlLoader = (URLClassLoader)this.getClass().getClassLoader();
        }

        cache.setParentLoader(urlLoader);
        cache.setClassRoot(scratchDirPath);
        cache.setJavaRoot(scratchDirPath);
        String xpRegistry = servletConfig.getInitParameter(XP_REGISTRY);
        if (xpRegistry == null){
            xpRegistry = "webapp:///WEB-INF/registry.xpreg";
        }
        cache.setXpRegistry(xpRegistry);
        String xpCacheAutoload = servletConfig.getInitParameter(XP_CACHE_AUTOLOAD);
        if (xpCacheAutoload == null){
            xpCacheAutoload = "true";
        }
        cache.setAutoLoad(xpCacheAutoload);
        cache.setClassPath(getClassPath(urlLoader,scratchDirPath));
        cache.setResolver(resolver);

        //////////////////////////////////////////
        // XSLT Cache setup
        //////////////////////////////////////////
        // Setup Stylesheet Factory
        // TODO: help garbage collector (servlet reloading)???
        String xslCacheSetting = (String) servletConfig.getInitParameter("XP_XSLT_CACHE");
        boolean enableCache = ! "FALSE".equalsIgnoreCase(xslCacheSetting); // default true
        templatesCache = new TemplatesCache(resolver);
        templatesCache.setCacheEnabled(enableCache);
    }

    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpXpContext xpContext = new HttpXpContextImpl(this,request,response);
        try{

            // TODO kinda redundant to construct and initialize with same parms
            // when the only difference is the "inititialize" method has just one extra instantiation in it
            // ** (jv) Actually, constructor should not be called, use factory instead.  When done, return to
            // factory for cleanup.
            xpContext.initialize(this,request,response);

            XpPage xpPage = getXpPage(getXpURIFromRequest(request.getServletPath()));
            if (xpPage == null){
//                  TODO replace with smarter error page
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            XMLReader xmlReader = new XpXMLReader(xpPage, xpContext);

            // If XSLT URI is specified, use it to transform output.  Otherwise, use identity transformer.
            // Propogate all <xp:output> settings when identity transformer is used.  Otherwise, only propogate
            // some of them.

            Properties xpOutputProperties = xpPage.getOutputProperties();
            String xsltPath = xpOutputProperties.getProperty(XpOutputKeys.XSLT_URI);
            boolean doFOP = "true".equalsIgnoreCase(xpOutputProperties.getProperty("doFOP"));
            Transformer transformer;
            if (doFOP) {
                if (xsltPath == null || "".equals(xsltPath)) {
                    transformer = templatesCache.getTransformer();
                } else {
                    org.anodyneos.commons.net.URI xslURI;
                    xslURI = new org.anodyneos.commons.net.URI(xsltPath);
                    transformer = templatesCache.getTransformer(xslURI);
                }
                response.setContentType("application/pdf");
                OutputStream out = response.getOutputStream();

                Driver driver = new Driver();
                driver.setRenderer(Driver.RENDER_PDF);
                driver.setOutputStream(out);
                Source source = new SAXSource(xmlReader, new InputSource(""));
                transformer.transform(source, new SAXResult(driver.getContentHandler()));
                return;
            } else if (xsltPath == null || "".equals(xsltPath)) {
                // TODO: allow encoding, indent, etc overrides to be declared in web.xml
                transformer = templatesCache.getTransformer();
                setTransformerProp( transformer, OutputKeys.CDATA_SECTION_ELEMENTS
                        ,xpOutputProperties.getProperty(XpOutputKeys.CDATA_SECTION_ELEMENTS));
                setTransformerProp( transformer, OutputKeys.DOCTYPE_PUBLIC
                        ,xpOutputProperties.getProperty(XpOutputKeys.DOCTYPE_PUBLIC));
                setTransformerProp( transformer, OutputKeys.DOCTYPE_SYSTEM
                        ,xpOutputProperties.getProperty(XpOutputKeys.DOCTYPE_SYSTEM));
                setTransformerProp( transformer, OutputKeys.ENCODING
                        ,xpOutputProperties.getProperty(XpOutputKeys.ENCODING));
                setTransformerProp( transformer, OutputKeys.INDENT
                        ,xpOutputProperties.getProperty(XpOutputKeys.INDENT));
                setTransformerProp( transformer, "{http://xml.apache.org/xslt}indent-amount"
                        ,xpOutputProperties.getProperty(XpOutputKeys.INDENT_AMOUNT));
                setTransformerProp( transformer, OutputKeys.MEDIA_TYPE
                        ,xpOutputProperties.getProperty(XpOutputKeys.MEDIA_TYPE));
                setTransformerProp( transformer, OutputKeys.METHOD
                        ,xpOutputProperties.getProperty(XpOutputKeys.METHOD));
                setTransformerProp( transformer, OutputKeys.OMIT_XML_DECLARATION
                        ,xpOutputProperties.getProperty(XpOutputKeys.OMIT_XML_DECLARATION));
            } else {
                try {
                    org.anodyneos.commons.net.URI xslURI;
                    xslURI = new org.anodyneos.commons.net.URI(xsltPath);

                    transformer = templatesCache.getTransformer(xslURI);
                    setTransformerProp( transformer, OutputKeys.ENCODING
                            ,xpOutputProperties.getProperty(XpOutputKeys.ENCODING));
                    setTransformerProp( transformer, OutputKeys.INDENT
                            ,xpOutputProperties.getProperty(XpOutputKeys.INDENT));
                    setTransformerProp( transformer, "{http://xml.apache.org/xslt}indent-amount"
                            ,xpOutputProperties.getProperty(XpOutputKeys.INDENT_AMOUNT));
                    setTransformerProp( transformer, OutputKeys.OMIT_XML_DECLARATION
                            ,xpOutputProperties.getProperty(XpOutputKeys.OMIT_XML_DECLARATION));

                } catch(FileNotFoundException fnf) {
                    throw new XpException("Unable to transform " + xpPage.getClass().getCanonicalName() + ".xp " +
                            "Check the xsltURI attribute of your xp file.  File not found: " + fnf.getMessage());

                } catch (URI.MalformedURIException mfe) {
                    throw new XpException("Unable to transform " + xpPage.getClass().getCanonicalName() + ".xp " +
                            "The xsltURI attribute of your xp file is invalid.  " + mfe.getMessage());
                }

            }

            output(request, response, xmlReader, transformer);

        }catch (TransformerConfigurationException tce){
            if (logger.isInfoEnabled()) {
                logger.info("XpServlet.service - " + tce.getMessage(),tce);
            }

        }catch (XpFileNotFoundException fnf){
            response.sendError(HttpServletResponse.SC_NOT_FOUND,request.getServletPath());

        }catch (XpException e){
            if (logger.isInfoEnabled()) {
                logger.info("XpServlet.service - 500 " );
            }

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

            e.printStackTrace();

        }catch (Exception e){
            if (logger.isInfoEnabled()) {
                logger.info("XpServlet.service - " + e.getMessage(),e);
            }
            throw new ServletException(e);
        }finally{
            xpContext.release();
        }


    }

    /**
     * @param loader
     * @param scratchDir
     * @return  the webapp classpath + scratchDir
     */
    private String getClassPath(URLClassLoader loader, String scratchDir){
        URL [] urls = loader.getURLs();
        StringBuffer cpath = new StringBuffer();

        for(int i=0; i<urls.length;i++) {
            URL url = urls[i];
            if( url.getProtocol().equals("file") ) {
                cpath.append((String)url.getFile()+File.pathSeparator);
            }
        }

        cpath.append(scratchDir);

        // The following is tomcat specific
        String tccp = (String) getServletContext().getAttribute("org.apache.catalina.jsp_classpath");
        if (null != tccp) {
            cpath.append(File.pathSeparator + tccp);
        }

        if (logger.isInfoEnabled()) {
            logger.info("XpServlet.getClassPath - " + cpath.toString());
        }

        return cpath.toString();
    }

    private URI getXpURIFromRequest(String servletPath){
        try{
            // there will already be a leading / in the servletPath
            URI uri = new URI("webapp://" + servletPath);
            return uri;
        }catch (Exception e){
            logger.error("XpServlet.getXpURIFromRequest - Unable to create URI from servletPath"
                    + e.getMessage(),e);
            return null;
        }
    }

    private XpPage getXpPage(URI xp) throws XpFileNotFoundException,XpTranslationException,XpCompilationException{
        return cache.getXpPage(xp);
    }

    //////////////////////////////////
    // XSLT stuff largely copied from proto project's ProtoResponse
    //////////////////////////////////

    public void output(HttpServletRequest req, HttpServletResponse res,
            Document page, Transformer transformer) throws IOException {
        output(req, res, new DOMSource(page), transformer);
    }

    public void output(HttpServletRequest req, HttpServletResponse res,
            InputSource page, Transformer transformer) throws IOException {
        output(req, res, new SAXSource(page), transformer);
    }

    public void output(HttpServletRequest req, HttpServletResponse res,
            XMLReader pageReader, Transformer transformer) throws IOException {
        output(req, res, new SAXSource(pageReader, new InputSource("")),
                transformer);
    }

    public void output(HttpServletRequest req, HttpServletResponse res,
            Source source, Transformer transformer) throws IOException {
        try {
            String method = transformer.getOutputProperty(OutputKeys.METHOD);
            String mediaType = transformer
                    .getOutputProperty(OutputKeys.MEDIA_TYPE);

            boolean doXHTMLMagic = false;

            if ("xml".equalsIgnoreCase(method)
                    && "application/xhtml+xml".equalsIgnoreCase(mediaType)) {
                doXHTMLMagic = true;
                // Don't do xhtmlMagic on Mozilla
                BrowserDetector bd = getBrowserDetector(req);
                if (BrowserDetector.MOZILLA.equals(bd.getBrowserName())
                        || BrowserDetector.SAFARI.equals(bd.getBrowserName())) {
                    doXHTMLMagic = false;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("User-Agent: " + req.getHeader("User-Agent"));
                    logger.debug("Browser name: " + bd.getBrowserName() + "; version: " + bd.getBrowserVersion());
                    logger.debug("doXHTMLMagic = " + doXHTMLMagic);
                }
            }

            if (doXHTMLMagic) {
                outputMagic(req, res, source, transformer);
            } else {
                outputNormal(req, res, source, transformer);
            }
        } catch (IOException e) {
            throw e;
        } catch (javax.xml.transform.TransformerConfigurationException e) {
            throw new IOException(e.getMessage());
        } catch (javax.xml.transform.TransformerException e) {
            throw new IOException(e.getMessage());
        }
    }

    private void outputNormal(HttpServletRequest req, HttpServletResponse res,
            Source source, Transformer transformer)
            throws TransformerConfigurationException, IOException,
            TransformerException {

        // process and output to client
        String contentType = null;
        if ((contentType = getContentType(transformer)) != null) {
            res.setContentType(contentType);
        }
        PrintWriter out = res.getWriter();
        String output = transformer.getOutputProperty(OutputKeys.METHOD);

        transformer.transform(source, new StreamResult(out));
        out.close();
    }

    private void outputMagic(HttpServletRequest req, HttpServletResponse res,
            Source source, Transformer transformer) throws IOException,
            TransformerException {
        // first is the XSL transformer (transformer)
        // then the namespace filter
        XMLFilterImpl nsFilter = new StripNamespaceFilter();
        // finally an identity transformer handler to serialize
        TransformerHandler th = templatesCache.getTransformerHandler();

        // setup identity th
        th.getTransformer().setOutputProperties(
                transformer.getOutputProperties());
        th.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
        th.getTransformer().setOutputProperty(OutputKeys.MEDIA_TYPE,
                "text/html");
        // th.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        String contentType = null;
        if ((contentType = getContentType(th.getTransformer())) != null) {
            res.setContentType(contentType);
        }

        // transformer outputs to nsFilter
        SAXResult saxResult = new SAXResult(nsFilter);
        // nsFilter outputs to th
        nsFilter.setContentHandler(th);
        // th outputs to browser
        PrintWriter out = res.getWriter();
        th.setResult(new StreamResult(out));

        // do it.
        transformer.transform(source, saxResult);
        out.close();
    }

    private String getContentType(Transformer transformer)
            throws TransformerConfigurationException {
        java.util.Properties props;
        props = transformer.getOutputProperties();
        if (props == null) {
            return null;
        } else {
            String encoding = props.getProperty("encoding");
            String media = props.getProperty("media-type");
            if (encoding != null) {
                return media + "; charset=" + encoding;
            } else {
                return media;
            }
        }
    }

    public BrowserDetector getBrowserDetector(HttpServletRequest req) {
        BrowserDetector browserDetector;
        String userAgent = req.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "";
        }
        browserDetector = new BrowserDetector(userAgent);
        return browserDetector;
    }

    private void setTransformerProp(Transformer trans, String prop, String value) {
        if(null != value && ! "".equals(value)) {
            trans.setOutputProperty(prop, value);
        }
    }

    private static String getStackTrace(Throwable thrown){
        if (thrown != null){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            thrown.printStackTrace(pw);
            return sw.toString();
        }else{
            return "";
        }

    }
}
