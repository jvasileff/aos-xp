package org.anodyneos.xpImpl.runtime;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.servlet.net.ServletContextURIHandler;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpFileNotFoundException;
import org.anodyneos.xp.XpPage;
import org.anodyneos.xp.http.HttpXpContext;
import org.anodyneos.xpImpl.http.HttpXpContextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XpServlet extends HttpServlet{

    private static final Log logger = LogFactory.getLog(XpServlet.class);

    private static final long serialVersionUID = 3258132440416794419L;

    public static final String IP_XP_REGISTRY = "xpRegistry";
    public static final String IP_XP_CACHE_AUTOLOAD = "xpCacheAutoload";
    public static final String IP_XSLT_CACHE = "xsltCache";

    private static final String TMP_DIR_ATTRIBUTE = "javax.servlet.context.tempdir";

    private XpFactoryImpl cache;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        //////////////////////////////////////////
        // XP Setup
        //////////////////////////////////////////

        // determine work directory for compiling XP pages
        File scratchDir = (File) getServletContext().getAttribute(TMP_DIR_ATTRIBUTE);
        if (!(scratchDir.exists() && scratchDir.canRead() && scratchDir.canWrite())){
            throw new ServletException("Work directory is invalid.  Check for existance and Read/Write settings.");
        }
        if (logger.isInfoEnabled()) { logger.info("xpClassRoot and xpJavaRoot: " + scratchDir.getPath()); }

        // configure URI resolvers
        UnifiedResolver resolver = new UnifiedResolver();
        resolver.setDefaultLookupEnabled(false);
        resolver.addProtocolHandler("classpath",
                new ClassLoaderURIHandler());
        resolver.addProtocolHandler("webapp",
                new ServletContextURIHandler(servletConfig.getServletContext()));

        // determine xpRegistry configuration file
        String xpRegistry = servletConfig.getInitParameter(IP_XP_REGISTRY);
        URI xpRegistryURI;
        try {
            if (xpRegistry == null){
                xpRegistry = "webapp:///WEB-INF/registry.xpreg";
            }
            if (logger.isInfoEnabled()) { logger.info("xpRegistry URI: " + xpRegistry); }
            xpRegistryURI = new URI(xpRegistry);
        } catch (URISyntaxException e) {
            throw new ServletException("Cannot create URI for xpRegistry '" + xpRegistry + "';", e);
        }

        // determine autoLoad setting
        boolean xpCacheAutoload = true; // default true
        String xpCacheAutoloadParam = servletConfig.getInitParameter(IP_XP_CACHE_AUTOLOAD);
        if (null != xpCacheAutoloadParam){
            xpCacheAutoload = Boolean.getBoolean(xpCacheAutoloadParam);
        }
        if (logger.isInfoEnabled()) { logger.info("xpCacheAutoload: " + xpCacheAutoload); }

        // configure the XpCachingLoader
        cache = XpFactoryImpl.getLoader();
        cache.setXpRegistryURI(xpRegistryURI);
        cache.setAutoLoad(xpCacheAutoload);
        cache.setResolver(resolver);
        cache.setJavaGenDirectory(scratchDir);
        cache.setClassGenDirectory(scratchDir);

        //////////////////////////////////////////
        // XSLT Setup
        //////////////////////////////////////////

        // determine xsltCache setting
        boolean xsltCache = true; // default
        String xsltCacheParam =  servletConfig.getInitParameter(IP_XSLT_CACHE);
        if (null != xsltCacheParam) {
            xsltCache = Boolean.getBoolean(xsltCacheParam);
        }
        if (logger.isInfoEnabled()) { logger.info("xsltCacheEnabled: " + xsltCache); }

        // configure the xslt templates cache
        cache.getTemplatesCache().setCacheEnabled(xsltCache);
    }

    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        HttpXpContext xpContext = null;

        try {

            // get XpPage
            URI xpURI = getXpURIFromRequest(req.getServletPath());
            XpPage xpPage = cache.newXpPage(xpURI);
            if (xpPage == null) {
                // TODO replace with smarter error page
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            // configure userAgent for xhtml auto method
            xpPage.configureForUserAgent(req.getHeader("User-Agent"));

            // set mimetype and encoding on the servlet response
            res.setContentType(genContentType(xpPage.getMediaType(), xpPage.getEncoding()));

            // setup xpContext
            // TODO: use factory instead
            xpContext = new HttpXpContextImpl(this,req,res);
            xpContext.initialize(this,req,res);

            // do it
            OutputStream out = res.getOutputStream();
            xpPage.service(xpContext, out);
            out.close();

        } catch (XpFileNotFoundException fnf) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND,req.getServletPath());

        } catch (XpException e) {
            if (logger.isInfoEnabled()) {
                logger.info("XpServlet.service - 500 " );
            }
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("XpServlet.service - " + e.getMessage(),e);
            }
            throw new ServletException(e);
        } finally {
            if (null != xpContext) {
                xpContext.release();
            }
        }

    }

    private URI getXpURIFromRequest(String servletPath){
        try {
            // there will already be a leading / in the servletPath
            URI uri = new URI("webapp://" + servletPath);
            return uri;
        } catch (Exception e) {
            logger.error("XpServlet.getXpURIFromRequest - Unable to create URI from servletPath"  + e.getMessage(),e);
            return null;
        }
    }

    private String genContentType(String media, String encoding) {
        if (encoding != null) {
            return media + "; charset=" + encoding;
        } else {
            return media;
        }
    }

}
