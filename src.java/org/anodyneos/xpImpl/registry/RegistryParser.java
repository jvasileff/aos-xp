package org.anodyneos.xpImpl.registry;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.commons.xml.sax.BaseParser;
import org.anodyneos.xp.tagext.TagLibraryInfo;
import org.anodyneos.xp.tagext.TagLibraryRegistry;
import org.anodyneos.xpImpl.tagext.TagLibraryRegistryImpl;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class RegistryParser extends BaseParser {

    public RegistryParser() {
        // super();
    }

    public static void main(String[] args) throws Exception {

        RegistryParser obj = new RegistryParser();
        long start = System.currentTimeMillis();
        //InputSource is = new InputSource(args[0]);
        InputSource is = new InputSource(new java.io.File(args[0]).toURL().toString());

        UnifiedResolver resolver = new UnifiedResolver();
        resolver.addProtocolHandler("classpath",
                new ClassLoaderURIHandler(Thread.currentThread().getContextClassLoader()));

        TagLibraryRegistry r = obj.process(is, resolver);
        TagLibraryInfo[] libs = r.getTagLibraryInfos();
        if (libs != null) {
            for (int i = 0; i < libs.length; i++) {
                System.out.println(libs[i].toString());
            }
        } else {
            System.out.println("No libraries.");
        }
        System.out.println("Completed in " + (System.currentTimeMillis() - start)
                + " milliseconds.");
    }

    public TagLibraryRegistry process(InputSource is, EntityResolver resolver) throws Exception {
        TagLibraryRegistryImpl registry = new TagLibraryRegistryImpl(resolver);
        RegistryContext ctx = new RegistryContext(is, registry);
        ProcessorRegistry p = new ProcessorRegistry(ctx);
        process(is, p, resolver);
        return registry;
    }

}
