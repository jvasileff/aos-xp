package org.anodyneos.xpImpl.runtime;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.commons.xml.xsl.TemplatesCache;
import org.anodyneos.commons.xml.xsl.TemplatesCacheImpl;
import org.anodyneos.servlet.xsl.GenericErrorHandler;
import org.anodyneos.xp.XpCompilationException;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpFileNotFoundException;
import org.anodyneos.xp.XpPage;
import org.anodyneos.xp.XpTranslationException;
import org.anodyneos.xpImpl.translater.Translater;
import org.anodyneos.xpImpl.translater.TranslaterResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XpCachingLoader{

    private static final Log logger = LogFactory.getLog(XpCachingLoader.class);

    public static final long NEVER_LOADED = -1;
    private ClassLoader parentLoader;
    private String classPath;
    private String classRoot;
    private String javaRoot;
    private String xpRegistry;
    private UnifiedResolver resolver;
    private boolean autoLoad = true;
    private TemplatesCache templatesCache;

    private final Map xpCache =
        Collections.synchronizedMap(new HashMap());
    private static XpCachingLoader me = new XpCachingLoader();

    private XpCachingLoader() {
        templatesCache = new TemplatesCacheImpl();
        GenericErrorHandler errorHandler = new GenericErrorHandler();
        templatesCache.setErrorHandler(errorHandler);
        templatesCache.setErrorListener(errorHandler);
        templatesCache.setCacheEnabled(true);
    }

    public static XpCachingLoader getLoader(){
        return me;
    }

    public XpPage getXpPage(URI xpURI) throws XpFileNotFoundException, XpTranslationException, XpCompilationException{

        XpPage xpPage = (XpPage)xpCache.get(xpURI.toString());
        long loadTime = NEVER_LOADED;
        if (xpPage != null ){
            loadTime = xpPage.getLoadTime();
        }

        // if the page hasn't been loaded before or it's out of date,
        // make sure we have exclusive access to the cache
        // then check again to be more certain that nobody has reloaded it since we last checked

        // TODO: jv note: technically, this may not work, see http://www.javaworld.com/jw-02-2001/jw-0209-double.html
        // not sure how much the weakness may just be theoretical, but we may want to take a closer look.
        if ((xpPage == null )
                || (xpPage != null && xpNeedsReloading(xpURI, loadTime, xpPage.getClass().getClassLoader()))){

            synchronized(this){

                xpPage = (XpPage)xpCache.get(xpURI.toString());
                loadTime = NEVER_LOADED;
                if (xpPage != null ){
                    loadTime = xpPage.getLoadTime();
                }
                // is the xp file even there any longer ?
                if (!Translater.xpExists(xpURI,getResolver())){
                    throw new XpFileNotFoundException(xpURI.toString());
                }

                // does it still need reloading (we could have spent a lot of time waiting for the lock) ?
                if ((xpPage == null )
                        || (xpPage != null && xpNeedsReloading(xpURI, loadTime, xpPage.getClass().getClassLoader()))){

                    if (logger.isInfoEnabled()) {
                        logger.info("reloading: " + xpURI.toString());
                    }
                    translateXp(xpURI);
                    compileXp(xpURI);
                    xpCache.remove(xpURI.toString());
                    xpPage = loadPage(xpURI);
                    xpCache.put(xpURI.toString(),xpPage);
                }
            }
        }

        return xpPage;

    }

    public XpRunner getXpRunner(URI xpURI) throws XpException {
        XpPage xpPage = getXpPage(xpURI);
        if (null == xpPage) {
            return null;
        } else {
            XpRunner xpRunner = new XpRunner(xpPage, xpURI, templatesCache);
            return xpRunner;
        }
    }

    private XpPage loadPage(URI xpURI) throws XpCompilationException{
        try{
            XpClassLoader loader = new XpClassLoader(parentLoader);
            loader.setRoot(getClassRoot());
            Class cls = loader.loadClass(Translater.getClassName(xpURI));
            return (XpPage)cls.newInstance();
        }catch(Exception e){
            throw new XpCompilationException(e);
        }
    }

    private boolean xpNeedsReloading(URI xpURI, long loadTime, ClassLoader loader) throws XpFileNotFoundException{
        if (this.isAutoLoad()){
            if (Translater.xpIsOutOfDate(xpURI,getClassRoot(),getResolver(),loadTime)) {
                return true;
            } else {

                try{
                    Class xpClass = Class.forName(Translater.getClassName(xpURI),true,loader);

                    Method getDependents = xpClass.getDeclaredMethod("getDependents",(Class[])null);

                    List dependents = (List)getDependents.invoke((Object)null,(Object[])null);

                    for (int i=0; i<dependents.size();i++){
                        String dependent = (String)dependents.get(i);
                        URI uriDep = new URI(dependent);
                        if (xpNeedsReloading(uriDep,loadTime,loader)){
                            return true;
                        }
                    }

                }catch (Exception e){
                    // something happened
                    if(logger.isErrorEnabled()) {
                        logger.error("Unable to inspect children of "
                                + xpURI.toString() + " to see if they would cause a reload.", e);
                    }
                    return true;
                }
            }
            // neither the file itself nor any dependents are out of date
            return false;
        }else{
            // autoload is not enabled, so never reload what's already there.
            return false;
        }
    }


    private void compileXp(URI xpURI) {
        if(logger.isDebugEnabled()) {
            logger.debug("compiling: " + xpURI.toString());
        }

        String[] args = new String[9];
        args[0] = "-classpath";
        args[1] = getClassPath();
        args[2] = "-sourcepath";
        args[3] = getJavaRoot();
        args[4] = "-d";
        args[5] = getClassRoot();
        args[6] = Translater.getJavaFile(getJavaRoot(),xpURI);
        args[7] = "-noExit";
        args[8] = "-nowarn";

        org.eclipse.jdt.internal.compiler.batch.Main.main(args);

        /*
        JavaCompiler compiler = new SunJavaCompiler(getClassPath(), getClassRoot());
        compiler.setSourcePath(getJavaRoot());
        compiler.compile(Translater.getJavaFile(getJavaRoot(),xpURI),System.err);
        */
    }

    private void translateXp(URI xpURI) throws IllegalStateException, XpTranslationException,XpFileNotFoundException{
        if(logger.isDebugEnabled()) {
            logger.debug("translating: " + xpURI.toString());
        }

        if (getResolver() == null){
            throw new IllegalStateException("XpCachingLoader requires resolver to be set.");
        }
        TranslaterResult result = Translater.translate(getJavaRoot(), xpURI, getXpRegistry(),resolver);
    }

    public String getClassRoot() {
        return classRoot;
    }
    public void setClassRoot(String classRoot) {
        this.classRoot = classRoot;
        refreshClassPath();
    }

    public ClassLoader getParentLoader() {
        return parentLoader;
    }
    public void setParentLoader(ClassLoader parentLoader) {
        this.parentLoader = parentLoader;
        refreshClassPath();
    }

    public String getJavaRoot() { return javaRoot; }
    public void setJavaRoot(String javaRoot) { this.javaRoot = javaRoot; }

    public String getXpRegistry() { return xpRegistry; }
    public void setXpRegistry(String xpRegistry) { this.xpRegistry = xpRegistry; }

    public String getClassPath() { return classPath; }
    //public void setClassPath(String classPath) { this.classPath = classPath; }

    public boolean isAutoLoad() { return autoLoad; }
    public void setAutoLoad(boolean autoLoad) { this.autoLoad = autoLoad; }

    public TemplatesCache getTemplatesCache() { return templatesCache; }
    public void setTemplatesCache(TemplatesCache templatesCache) { this.templatesCache = templatesCache; }

    public UnifiedResolver getResolver() {
        return resolver;
    }
    public void setResolver(UnifiedResolver resolver) {
        this.resolver = resolver;
        templatesCache.setUnifiedResolver(resolver);
    }

    protected void refreshClassPath() {

        StringBuffer cpath = new StringBuffer();

        for (URLClassLoader loopLoader = (URLClassLoader) this.parentLoader;
                loopLoader != null;
                loopLoader = (URLClassLoader) loopLoader.getParent()) {
            URL[] urls = loopLoader.getURLs();
            if (logger.isDebugEnabled()) {
                logger.debug("adding URLClassloader URLs:" + Arrays.asList(urls));
            }
            for(int i=0; i<urls.length;i++) {
                URL url = urls[i];
                if( url.getProtocol().equals("file") ) {
                    cpath.append((String)url.getFile()+File.pathSeparator);
                }
            }
        }

        // The following is tomcat specific, produces the same result as climbing the classloader chain on 5.5
        /*
        String tccp = (String) getServletContext().getAttribute("org.apache.catalina.jsp_classpath");
        logger.debug("Tomcat servlet classpath: " + tccp);
        if (null != tccp) {
            cpath.append(File.pathSeparator + tccp);
        }
        */

        if (null != classRoot) {
            cpath.append(classRoot);
        }

        if (logger.isInfoEnabled()) {
            logger.info("now using classpath: " + cpath.toString());
        }

        this.classPath = cpath.toString();
    }

}
