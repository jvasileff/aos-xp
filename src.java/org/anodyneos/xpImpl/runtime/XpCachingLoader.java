package org.anodyneos.xpImpl.runtime;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anodyneos.xp.XpPage;
import org.anodyneos.xpImpl.compiler.JavaCompiler;
import org.anodyneos.xpImpl.compiler.SunJavaCompiler;
import org.anodyneos.xpImpl.translater.Translater;
import org.anodyneos.xpImpl.translater.TranslaterContext;
import org.anodyneos.xpImpl.translater.TranslaterResult;
import org.anodyneos.xpImpl.runtime.XpClassLoader;


public class XpCachingLoader extends ClassLoader{
    private ClassLoader parentLoader;
    private String classPath;
    private String classRoot;
    private String javaRoot;
    private String xpRoot;
    private String xpRegistry;
    private final Map xpCache =
        Collections.synchronizedMap(new HashMap());
    private static XpCachingLoader me = new XpCachingLoader();

    private XpCachingLoader(){}

    public static XpCachingLoader getLoader(){
        return me;
    }

    public Class< ? > loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith(TranslaterContext.DEFAULT_PACKAGE + ".")){
            return getXpPage(name).getClass();
        }else{
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        }
    }
    public XpPage getXpPage(String xpName){
        try{
            if (xpNeedsReloading(xpName)){
                translateXp(xpName);
                compileXp(xpName);
                xpCache.remove(xpName);
            }

            XpPage xpPage = (XpPage)xpCache.get(xpName);

            if (xpPage == null){
                xpPage = loadPage(xpName);
                xpCache.put(xpName,xpPage);
            }

            return xpPage;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private XpPage loadPage(String xpName) throws Exception {
        XpClassLoader loader = new XpClassLoader(this);
        loader.setRoot(getClassRoot());
        Class cls = loader.loadClass(getXpClassName(xpName));
        return (XpPage)cls.newInstance();
    }

    public static String getXpClassName(String xpFileName){
        String retVal = xpFileName.replaceFirst("\\.xp","").replace('/','.');
        if (retVal.startsWith(".")){
            retVal = retVal.substring(1,retVal.length());
        }
        if (!retVal.startsWith(TranslaterContext.DEFAULT_PACKAGE + ".")){
            retVal = TranslaterContext.DEFAULT_PACKAGE + "." + retVal;
        }
        return retVal;
    }
    private boolean xpNeedsReloading(String xpFileName){
        File xpFile = new File (getXpFileName(xpFileName));
        File classFile = new File(getClassFileName(xpFileName));
        if (classFile.exists()){
            if (xpFile.exists()){

                if (classFile.lastModified() >= xpFile.lastModified()){
                    // the class file is up to date,check dependents
                    try{
                        XpPage xpPage = (XpPage)xpCache.get(xpFileName);

                        if (xpPage == null){
                            xpPage = loadPage(xpFileName);
                        }

                        List dependents = xpPage.getDependents();
                        for (int i=0;i<dependents.size();i++){
                            String dependent = (String)dependents.get(i);
                            if (xpNeedsReloading(dependent)){
                                return true;
                            }
                        }
                    }catch (Exception e){
                            e.printStackTrace();
                            return false;
                    }
                    // none of the dependents are out of date, no need to reload
                    return false;

                }else{
                    // the source file is newer than the class file
                    return true;
                }

            }else{
                // the class file exists, but there's no source.  Let's just work with what we've got
                return false;
            }
        }else{
            // the class file does not exist, and we have the source
            if (xpFile.exists()){
                return true;
            }else{
                // no class file and no source, tough luck
                return false;
            }
        }
    }

    private void compileXp(String xpName) {

        String classpath = System.getProperty("java.class.path");

        classpath += File.pathSeparator + getClassRoot();
        classpath += File.pathSeparator + getClassPath();

        JavaCompiler compiler = new SunJavaCompiler(classpath,getClassRoot());

        compiler.compile(getJavaFileName(xpName),System.err);
    }

    private void translateXp(String xpName) throws Exception{
        TranslaterResult result = Translater.translate(getXpRoot(),
                                                getXpFileName(xpName), getJavaFileName(xpName),getXpRegistry());

        List dependents = result.getDependents();

        for (int i=0; i<dependents.size();i++){
            String dependent = (String)dependents.get(i);
            // TODO figure out a way to prevent/detect circular references
            if (xpNeedsReloading(dependent)){
                translateXp(dependent);
                compileXp(dependent);
            }
        }
    }

    private String getClassFileName(String xpName){
        return (getClassRoot() + TranslaterContext.DEFAULT_PACKAGE + "/" + xpName.replaceFirst("\\.xp",".class"));
    }

    private String getJavaFileName(String xpName){
        String temp = xpName.replaceFirst("\\.xp",".java");
        if (temp.startsWith("/")){
            temp = temp.substring(1,temp.length());
        }
        return (getJavaRoot() + TranslaterContext.DEFAULT_PACKAGE + "/" + temp);
    }
    private String getXpFileName(String xpName){
        return (getXpRoot() + xpName);
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
    public String getXpRoot() {
        return xpRoot;
    }
    public void setXpRegistry(String xpRegistry) {
        this.xpRegistry = xpRegistry;
    }
    public void setXpRoot(String xpRoot) {
        this.xpRoot = xpRoot;
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
}
