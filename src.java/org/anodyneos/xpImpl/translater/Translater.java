package org.anodyneos.xpImpl.translater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.commons.xml.sax.BaseParser;
import org.anodyneos.xp.tagext.TagLibraryRegistry;
import org.anodyneos.xpImpl.registry.RegistryParser;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.xml.sax.InputSource;
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
            OutputStream os;
            Translater obj = new Translater();
            long start = System.currentTimeMillis();

            translate(args[CL_XP_ROOT],args[CL_XP_PAGE],args[CL_JAVA_FILE],args[CL_REGISTRY_FILE]);

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

    public static TranslaterResult translate(String xpRoot, String xpPage, String javaFile, String registryFile) throws Exception{
        OutputStream os;
        Translater obj = new Translater();

        // registry
        UnifiedResolver resolver = new UnifiedResolver();
        resolver.addProtocolHandler("classpath",
                new ClassLoaderURIHandler(RegistryParser.class.getClassLoader()));
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

        TranslaterResult result = obj.process(new InputSource(xpPage), os, registry, className);
        os.close();

        return result;
    }

    private static boolean createDir(String javaFile){
        boolean created = false;
        System.out.println("javaFile = " + javaFile);
        String filePath = javaFile.replaceFirst("[/\\\\]\\w*\\.java","");

        File dir = new File(filePath);
        System.out.println("Creating " + dir.getAbsolutePath());
        if (!dir.exists()){
            created = dir.mkdirs();
        }else{
            created = true;
        }

        return created;
    }

    public TranslaterResult process(InputSource is, OutputStream os,
            TagLibraryRegistry taglibRegistry, String fullClassName) throws Exception {
        CodeWriter out = new CodeWriter(os);
        TranslaterContext ctx = new TranslaterContext(is, out, taglibRegistry);
        ctx.setFullClassName(TranslaterContext.DEFAULT_PACKAGE + "."+fullClassName);
        TranslaterProcessor p = new ProcessorPage(ctx);
        process(is, p);
        out.flush();

        return (TranslaterResult) ctx;
    }

}
