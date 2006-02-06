package org.anodyneos.xpImpl.translater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.List;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.net.URI;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.commons.xml.sax.BaseParser;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpFileNotFoundException;
import org.anodyneos.xp.XpTranslationException;
import org.anodyneos.xp.tagext.TagLibraryRegistry;
import org.anodyneos.xpImpl.registry.RegistryParser;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Translater extends BaseParser {

    private static final int CL_XP_ROOT = 0;
    private static final int CL_XP_PAGE = 1;
    private static final int CL_JAVA_FILE = 2;
    private static final int CL_REGISTRY_FILE = 3;

    public Translater() {
        // super();
    }

    public static void main(String[] args) throws Exception {
        try {
            long start = System.currentTimeMillis();
            UnifiedResolver resolver = new UnifiedResolver();
            resolver.addProtocolHandler("classpath",
                    new ClassLoaderURIHandler(Thread.currentThread().getContextClassLoader()));

            translate(args[CL_XP_ROOT],args[CL_XP_PAGE],args[CL_JAVA_FILE],args[CL_REGISTRY_FILE],resolver);

            System.out.println("Completed in " + (System.currentTimeMillis() - start) + " milliseconds.");
        } catch (SAXParseException e) {
            System.err.print(e.getSystemId());
            System.err.print(":" + e.getLineNumber() + ":" + e.getColumnNumber() + ": ");
            System.err.print(e.getLocalizedMessage());

            if (null != e.getException()) {
                System.err.println();
                System.err.println("Cause: ");
                e.printStackTrace(System.err);
            }
        }
    }

    public static TranslaterResult translate(String xpRoot, String xpPage,
            String javaFile, String registryFile, EntityResolver er) throws Exception{
        OutputStream os;
        Translater obj = new Translater();

        // registry
        UnifiedResolver resolver = new UnifiedResolver();
        resolver.addProtocolHandler("classpath",
                new ClassLoaderURIHandler(Thread.currentThread().getContextClassLoader()));
        InputSource is = new InputSource(new java.io.File(registryFile).toURL().toString());
        TagLibraryRegistry registry = new RegistryParser().process(is, resolver);

        // add a / if it doesn't exist
        char lastChar = xpRoot.charAt(xpRoot.length()-1);
        if ( lastChar != '/' && lastChar != '\\'){
            xpRoot += File.separatorChar;
        }
        // translate codegen
        String className = xpPage;
        className = className.substring(xpRoot.length(),className.length());
        if (className.startsWith("/")){
            className = className.substring(1,className.length());
        }
        className = className.replace('/','.');
        className = className.replace('\\','.');
        className = className.substring(0, className.lastIndexOf('.'));

        if (createDir(javaFile)){
            os = new FileOutputStream(javaFile);
        }else{
            throw new Exception("Unable to create file: " + javaFile +
                    " because the directory structure could not be created.");
        }

        TranslaterResult result = obj.process(new InputSource(xpPage), os, registry, className, er);
        os.close();

        return result;
    }

    /**
     * @param xpRoot - The physical directory where the xp are located
     * @param tempRoot - The physical directory where the java file is to be based
     * @param xpURI - The URI of the desired xp
     * @param registryFile - RegistryFile of TLDs
     * @param resolver - UnifiedResolver
     * @return TranslaterResult
     * @throws Exception
     */
    public static TranslaterResult translate(String tempRoot, URI xpURI, String registryFile, UnifiedResolver resolver)
    throws XpFileNotFoundException,XpTranslationException{
        Translater obj = new Translater();
        try{
            InputSource is = resolver.resolveEntity(null,registryFile);
            TagLibraryRegistry registry = new RegistryParser().process(is, resolver);

            return obj.translate(tempRoot,xpURI,registry,resolver);

        }catch(XpFileNotFoundException fnf){
            throw fnf;
        }catch(XpTranslationException te){
            throw te;
        }catch (Exception e){
            throw new XpTranslationException(e);
        }
    }

    public TranslaterResult translate(String tempRoot, URI xpURI, TagLibraryRegistry registry, UnifiedResolver resolver)
    throws XpFileNotFoundException, XpTranslationException{

        try{
            InputSource xpSource = resolver.resolveEntity(null,xpURI.toString());
            if (xpSource == null){
                throw new XpTranslationException("File Not Found: " + xpURI.toString());
            }
            String javaFile = getJavaFile(tempRoot, xpURI);

            String className = getClassName(xpURI); // i.e. xp.WEB_INF.common.header

            OutputStream os;
            if (createDir(javaFile)){
                os = new FileOutputStream(javaFile);
            }else{
                throw new XpTranslationException("Unable to create file: " + javaFile +
                        " because the directory structure could not be created.");
            }

            TranslaterResult result = process(xpSource, os, registry, className, resolver);
            os.close();

            // now translate the dependents (There is no check to see if they are out of date)
            // this is necessary in order to obtain the dependent list
            List dependents = result.getDependents();
            for (int i=0; i<dependents.size();i++){
                String dependent = (String)dependents.get(i);
                URI uriDep = new URI(dependent);
                translate(tempRoot,uriDep,registry,resolver);
            }

            return result;
        }catch (IOException ioe){
            throw new XpTranslationException(ioe);
        }catch (SAXException se){
            if (se.getException() != null){
                throw new XpTranslationException(se.getException());
            }else if (se.getCause() != null){
                throw new XpTranslationException(se.getCause());
            }else{
                throw new XpTranslationException(se);
            }
        }
    }


    public TranslaterResult process(InputSource is, OutputStream os, TagLibraryRegistry taglibRegistry,
            String fullClassName, EntityResolver er) throws SAXException, IOException {
        CodeWriter out = new CodeWriter(os);
        TranslaterContext ctx = new TranslaterContext(is, out, taglibRegistry);
        ctx.setFullClassName(fullClassName);
        TranslaterProcessor p = new ProcessorPage(ctx);
        process(is, p, er);
        out.flush();

        return (TranslaterResult) ctx;
    }

    /**
     * Check to see if xp is newer than java
     * @param xpURI
     * @param tempRoot
     * @param resolver
     * @return
     */
    public static boolean xpNeedsTranslating(URI xpURI, String tempRoot, UnifiedResolver resolver)
    throws XpException{

        File javaFile = new File(getJavaFile(tempRoot,xpURI));
        if (javaFile.exists()){
            return xpIsOutOfDate(xpURI,tempRoot,resolver,javaFile.lastModified());
        }else{
            // the java file does not exist, so the xp needs translating
            return true;
        }
    }

    /**
     * Check to see if xp is newer than class
     *
     * @param xpURI
     * @param tempRoot
     * @param resolver
     * @return
     */
    public static boolean xpNeedsCompiling(URI xpURI, String tempRoot, UnifiedResolver resolver)
        throws XpException{

        File javaFile = new File(getClassFile(tempRoot,xpURI));
        if (javaFile.exists()){
                return xpIsOutOfDate(xpURI,tempRoot,resolver,javaFile.lastModified());
        }else{
            // the java file does not exist, so the xp needs translating
            return true;
        }
    }

    public static boolean xpIsOutOfDate(URI xpURI, String tempRoot, UnifiedResolver resolver, long loadTime)
        throws XpFileNotFoundException{

        try{
            URLConnection conn = resolver.openConnection(xpURI);
            if (conn == null){
                throw new IOException(xpURI.toString() + " does not exist.");
            }
            long xpLastModified = conn.getLastModified();

            if (loadTime >= xpLastModified){
                return false;

            }else{
                return true;
            }

        }catch (IOException ioe){
            throw new XpFileNotFoundException(xpURI.toString());
        }
    }

    public static boolean xpExists(URI xpURI, UnifiedResolver resolver){
        try{
            URLConnection conn = resolver.openConnection(xpURI);
            if (conn == null){
                return false;
            }else{
                return true;
            }

        }catch (IOException ioe){
            return false;
        }
    }

    private static String stripXpExtensionFromPath(String xpPath){
        if (xpPath.trim().toUpperCase().endsWith(".XP")){
            String trimmedPath = xpPath.trim();
            return trimmedPath.substring(0,trimmedPath.length()-3);
        }else{
            return xpPath;
        }
    }

    public static String getJavaFile(String tempRoot, URI xpURI){
        String fullPath = concatPaths(TranslaterContext.DEFAULT_PACKAGE + "/",xpURI.getPath());
        fullPath = stripXpExtensionFromPath(fullPath);
        fullPath = fullPath.replace('-','_');   // TODO replace with more robust replacement
        fullPath = fullPath.replace('.','_');   // TODO replace with more robust replacement
        fullPath += ".java";
        fullPath = concatPaths(tempRoot,fullPath);

        return fullPath;

    }
    public static String getClassFile(String tempRoot, URI xpURI){
        String fullPath = concatPaths(TranslaterContext.DEFAULT_PACKAGE + "/",xpURI.getPath());
        fullPath = stripXpExtensionFromPath(fullPath);
        fullPath = fullPath.replace('-','_');   // TODO replace with more robust replacement
        fullPath = fullPath.replace('.','_');   // TODO replace with more robust replacement
        fullPath += ".class";
        fullPath = concatPaths(tempRoot,fullPath);

        return fullPath;

    }



    /**
     *
     * Convert webapp:///WEB-INF/xp/account/account_list.query.xp into xp.WEB_INF.xp.account.account_list_query
     *
     * @param xpURI
     * @return String
     */
    public static String getClassName(URI xpURI){
        String fullPath = xpURI.getPath();
        fullPath = stripXpExtensionFromPath(fullPath);
        fullPath = fullPath.replace('-','_');   // TODO replace with more robust replacement
        fullPath = fullPath.replace('.','_');   // TODO replace with more robust replacement
        fullPath = concatPaths(TranslaterContext.DEFAULT_PACKAGE + "/",fullPath);
        fullPath = fullPath.replace('/','.');
        return fullPath;

    }

    /**
     * concats pathPrefix and pathSuffix, with a / between the two
     *
     * @param pathPrefix
     * @param pathSuffix
     * @return pathPrefix + pathSuffix
     * @throws Exception
     */
    public static String concatPaths(String pathPrefix, String pathSuffix){
        String c = null;
        if (pathPrefix == null){
            c = pathSuffix;
        }else if (pathSuffix == null){
            c = pathPrefix;
        }else{
            // start with the prefix
            c = pathPrefix;
            if (pathPrefix.endsWith("/")){
                if (pathSuffix.startsWith("/")){
                    // if the prefix ends in / and the suffix begins with / then strip the / from the suffix
                    pathSuffix = pathSuffix.substring(1,pathSuffix.length());
                }
            }else{
                if (!pathSuffix.startsWith("/")){
                    // if the prefix does NOT end in / and the suffix does NOT begin with / then append a / to c
                    c = c.concat("/");
                }
            }
            // and add then concatenate the two together
            c = c.concat(pathSuffix);
        }
        return c;
    }

    private static boolean createDir(String javaFile){

        boolean created = false;
        String filePath = javaFile.replaceFirst("[/\\\\]\\w*\\.java","");
        File dir = new File(filePath);
        if (!dir.exists()){
            created = dir.mkdirs();
        }else{
            created = true;
        }

        return created;
    }

}
