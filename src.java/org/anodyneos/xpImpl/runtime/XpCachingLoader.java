package org.anodyneos.xpImpl.runtime;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anodyneos.commons.net.URI;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.xp.XpPage;
import org.anodyneos.xpImpl.compiler.JavaCompiler;
import org.anodyneos.xpImpl.compiler.SunJavaCompiler;
import org.anodyneos.xpImpl.runtime.exception.XpCompilationException;
import org.anodyneos.xpImpl.runtime.exception.XpFileNotFoundException;
import org.anodyneos.xpImpl.runtime.exception.XpTranslationException;
import org.anodyneos.xpImpl.translater.Translater;
import org.anodyneos.xpImpl.translater.TranslaterResult;

public class XpCachingLoader{
    public static final long NEVER_LOADED = -1;
    private ClassLoader parentLoader;
    private String classPath;
    private String classRoot;
    private String javaRoot;
    private String xpRegistry;
    private UnifiedResolver resolver;

    private final Map xpCache =
        Collections.synchronizedMap(new HashMap());
    private static XpCachingLoader me = new XpCachingLoader();

    private XpCachingLoader(){}

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
                    throw new XpFileNotFoundException();
                }

                // does it still need reloading (we could have spent alot of time waiting for the lock) ?
                if ((xpPage == null )
                        || (xpPage != null && xpNeedsReloading(xpURI, loadTime, xpPage.getClass().getClassLoader()))){
                    System.out.println(xpURI.toString() + " needs reloading");
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
                System.out.println("Unable to inspect children of "
                        + xpURI.toString() + " to see if they would cause a reload.");
                e.printStackTrace();
                return true;
            }
        }
        // neither the file itself nor any dependents are out of date
        return false;

    }


    private void compileXp(URI xpURI) {
        System.out.println("Compiling " + xpURI.toString());

        JavaCompiler compiler = new SunJavaCompiler(getClassPath(),getClassRoot());

        compiler.setSourcePath(getJavaRoot());
        compiler.compile(Translater.getJavaFile(getJavaRoot(),xpURI),System.err);
    }

    private void translateXp(URI xpURI) throws IllegalStateException, XpTranslationException,XpFileNotFoundException{
        System.out.println("Translating " + xpURI.toString());

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
    }
    public String getJavaRoot() {
        return javaRoot;
    }
    public void setJavaRoot(String javaRoot) {
        this.javaRoot = javaRoot;
    }
    public String getXpRegistry() {
        return xpRegistry;
    }
    public void setXpRegistry(String xpRegistry) {
        this.xpRegistry = xpRegistry;
    }
    public String getClassPath() {
        return classPath;
    }
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }
    public ClassLoader getParentLoader() {
        return parentLoader;
    }
    public void setParentLoader(ClassLoader parentLoader) {
        this.parentLoader = parentLoader;
    }

    /**
     * @return Returns the resolver.
     */
    public UnifiedResolver getResolver() {
        return resolver;
    }
    /**
     * @param resolver The resolver to set.
     */
    public void setResolver(UnifiedResolver resolver) {
        this.resolver = resolver;
    }
}
