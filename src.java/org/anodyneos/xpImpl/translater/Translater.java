package org.anodyneos.xpImpl.translater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.commons.xml.sax.BaseParser;
import org.anodyneos.xp.tagext.TagLibraryRegistry;
import org.anodyneos.xpImpl.registry.RegistryParser;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

public class Translater extends BaseParser {

    public Translater() {
        // super();
    }

    public static void main(String[] args) throws Exception {
        try {
            OutputStream os;
            Translater obj = new Translater();
            long start = System.currentTimeMillis();

            // registry
            UnifiedResolver resolver = new UnifiedResolver();
            resolver.addProtocolHandler("classpath",
                    new ClassLoaderURIHandler(RegistryParser.class.getClassLoader()));
            InputSource is = new InputSource(new java.io.File(args[2]).toURL().toString());
            TagLibraryRegistry registry = new RegistryParser().process(is, resolver);

            // translate codegen
            File inputFile = new File(args[0]);
            String className = inputFile.getName();
            className = className.substring(0, className.indexOf('.'));
            os = new FileOutputStream(args[1]);

            obj.process(new InputSource(args[0]), os, registry, className);
            os.close();
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

    public TranslaterResult process(InputSource is, OutputStream os,
            TagLibraryRegistry taglibRegistry, String fullClassName) throws Exception {
        CodeWriter out = new CodeWriter(os);
        TranslaterContext ctx = new TranslaterContext(is, out, taglibRegistry);
        ctx.setFullClassName(fullClassName);
        TranslaterProcessor p = new ProcessorPage(ctx);
        process(is, p);
        out.flush();
        return (TranslaterResult) ctx;
    }

}
