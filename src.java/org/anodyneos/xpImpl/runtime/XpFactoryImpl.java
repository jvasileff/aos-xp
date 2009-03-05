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
import org.anodyneos.xp.XpFactory;
import org.anodyneos.xp.XpFileNotFoundException;
import org.anodyneos.xp.XpPage;
import org.anodyneos.xp.XpTranslationException;
import org.anodyneos.xpImpl.translater.Translater;
import org.anodyneos.xpImpl.translater.TranslaterResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XpFactoryImpl extends XpFactory {

    private static final Log log = LogFactory.getLog(XpFactoryImpl.class);

    public static final long NEVER_LOADED = -1;
    private ClassLoader parentLoader;
    private String classPath;
    private File classRoot;
    private File javaRoot;
    private URI xpRegistry;
    private UnifiedResolver resolver;
    private boolean autoLoad = true;
    private TemplatesCache templatesCache;

    private final Map xpCache =
        Collections.synchronizedMap(new HashMap());
    private static XpFactoryImpl me = new XpFactoryImpl();

    private XpFactoryImpl() {
        templatesCache = new TemplatesCacheImpl();
        GenericErrorHandler errorHandler = new GenericErrorHandler();
        templatesCache.setErrorHandler(errorHandler);
        templatesCache.setErrorListener(errorHandler);
        templatesCache.setCacheEnabled(true);

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = this.getClass().getClassLoader();
        }
        setParentLoader(loader);
    }

    public static XpFactoryImpl getLoader(){
        return me;
    }

    public XpPage newXpPage(URI xpURI)
    throws XpFileNotFoundException, XpTranslationException, XpCompilationException, XpException {

        //XpPage xpPage = (XpPage)xpCache.get(xpURI.toString());
        XpPageHolder xpPageHolder = (XpPageHolder) xpCache.get(xpURI.toString());
        long loadTime = NEVER_LOADED;
        if (xpPageHolder != null ){
            loadTime = xpPageHolder.loadTime;
        }

        // if the page hasn't been loaded before or it's out of date,
        // make sure we have exclusive access to the cache
        // then check again to be more certain that nobody has reloaded it since we last checked

        // TODO: jv note: technically, this may not work, see http://www.javaworld.com/jw-02-2001/jw-0209-double.html
        // not sure how much the weakness may just be theoretical, but we may want to take a closer look.
        if ((xpPageHolder == null )
                || (xpPageHolder != null
                        && xpNeedsReloading(xpURI, loadTime, xpPageHolder.xpPageClass.getClassLoader()))){

            synchronized(this){

                xpPageHolder = (XpPageHolder)xpCache.get(xpURI.toString());
                loadTime = NEVER_LOADED;
                if (xpPageHolder != null ){
                    loadTime = xpPageHolder.loadTime;
                }
                // is the xp file even there any longer ?
                // TODO this should be checked above, not just when reloading is required (also dependents)
                if (!Translater.xpExists(xpURI,getResolver())){
                    // TODO xpCache.remove(xpURI.toString());
                    throw new XpFileNotFoundException(xpURI.toString());
                }

                // does it still need reloading (we could have spent a lot of time waiting for the lock) ?
                if ((xpPageHolder == null )
                        || (xpPageHolder != null
                                && xpNeedsReloading(xpURI, loadTime, xpPageHolder.getClass().getClassLoader()))) {

                    if (log.isInfoEnabled()) {
                        log.info("reloading: " + xpURI.toString());
                    }
                    translateXp(xpURI);
                    compileXp(xpURI);
                    xpCache.remove(xpURI.toString());
                    xpPageHolder = loadPage(xpURI);
                    xpCache.put(xpURI.toString(),xpPageHolder);
                }
            }
        }

        try {
            AbstractXpPage xpPage = (AbstractXpPage) xpPageHolder.xpPageClass.newInstance();
            xpPage.setTemplatesCache(getTemplatesCache());
            xpPage.init();
            return xpPage;
        } catch (IllegalAccessException e) {
            throw new XpCompilationException(e);
        } catch (InstantiationException e) {
            throw new XpCompilationException(e);
        }
    }

    private XpPageHolder loadPage(URI xpURI) throws XpCompilationException{
        try{
            XpPageHolder xpPageHolder = new XpPageHolder();
            xpPageHolder.loadTime = System.currentTimeMillis();
            XpClassLoader loader = new XpClassLoader(parentLoader);
            loader.setRoot(getClassGenDirectory().getPath());
            xpPageHolder.xpPageClass = loader.loadClass(Translater.getClassName(xpURI));
            return xpPageHolder;
        }catch(Exception e){
            throw new XpCompilationException(e);
        }
    }

    private boolean xpNeedsReloading(URI xpURI, long loadTime, ClassLoader loader) throws XpFileNotFoundException{
        if (this.isAutoLoad()){
            if (Translater.xpIsOutOfDate(xpURI, getClassGenDirectory().getPath(), getResolver(), loadTime)) {
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
                    if(log.isErrorEnabled()) {
                        log.error("Unable to inspect children of "
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
        if(log.isDebugEnabled()) {
            log.debug("compiling: " + xpURI.toString());
        }

        String[] args = new String[9];
        args[0] = "-classpath";
        args[1] = getCompileClassPath();
        args[2] = "-sourcepath";
        args[3] = getJavaGenDirectory().getPath();
        args[4] = "-d";
        args[5] = getClassGenDirectory().getPath();
        args[6] = Translater.getJavaFile(getJavaGenDirectory().getPath(), xpURI);
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
        if(log.isDebugEnabled()) {
            log.debug("translating: " + xpURI.toString());
        }

        if (getResolver() == null){
            throw new IllegalStateException("XpCachingLoader requires resolver to be set.");
        }
        // TODO parse the registry separately, check it on each page load.
        TranslaterResult result = Translater.translate(
                getJavaGenDirectory().getPath(), xpURI, getXpRegistryURI().toString(),resolver);
    }

    public File getClassGenDirectory() {
        return classRoot;
    }
    public void setClassGenDirectory(File classRoot) {
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

    public File getJavaGenDirectory() { return javaRoot; }
    public void setJavaGenDirectory(File javaRoot) { this.javaRoot = javaRoot; }

    public URI getXpRegistryURI() { return xpRegistry; }
    public void setXpRegistryURI(URI xpRegistry) { this.xpRegistry = xpRegistry; }

    public String getCompileClassPath() { return classPath; }

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

    private void refreshClassPath() {

        StringBuffer cpath = new StringBuffer();

        for (URLClassLoader loopLoader = (URLClassLoader) this.parentLoader;
                loopLoader != null;
                loopLoader = (URLClassLoader) loopLoader.getParent()) {
            URL[] urls = loopLoader.getURLs();
            if (log.isDebugEnabled()) {
                log.debug("adding URLClassloader URLs:" + Arrays.asList(urls));
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

        if (log.isDebugEnabled()) {
            log.debug("now using classpath: " + cpath.toString());
        }

        this.classPath = cpath.toString();
    }

    private class XpPageHolder {
        private Class xpPageClass;
        private long loadTime;
    }

}
