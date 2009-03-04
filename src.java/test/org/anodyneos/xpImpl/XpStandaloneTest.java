package test.org.anodyneos.xpImpl;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.net.URLChangeRootURIHandler;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.standalone.StandaloneXpContext;
import org.anodyneos.xpImpl.runtime.XpCachingLoader;
import org.anodyneos.xpImpl.runtime.XpRunner;
import org.anodyneos.xpImpl.standalone.StandaloneXpContextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XpStandaloneTest {

    private static final Log log = LogFactory.getLog(XpStandaloneTest.class);

    private static final long serialVersionUID = 1L;

    private XpCachingLoader cache;
    private String xpSourceDirectory;
    private String xpRegistryFile;
    private String scratchJavaDirectory;
    private String scratchClassDirectory;

    public static void main(String[] args) throws Exception {
        log.info("adsfasdf");
        XpStandaloneTest obj = new XpStandaloneTest();
        obj.setScratchJavaDirectory(args[0]);
        obj.setScratchClassDirectory(args[1]);
        obj.setXpSourceDirectory(args[2]);
        obj.setXpRegistryFile(args[3]);
        obj.init();

        StandaloneXpContext ctx = new StandaloneXpContextImpl();
        ctx.initialize();
        try {
            obj.service(ctx, new URI(args[4]));
        } finally {
            ctx.release();
        }
    }

    public void init() throws Exception {

        // TODO: perform this check in XpCachingLoader
        File scratchJavaDir = new File(getScratchJavaDirectory());
        if (!(scratchJavaDir.exists() && scratchJavaDir.canRead() && scratchJavaDir.canWrite())) {
            throw new Exception("Work directory is invalid.  Check for existance and Read/Write settings.");
        }
        String scratchJavaDirPath = scratchJavaDir.getAbsolutePath();
        File scratchClassDir = new File(getScratchClassDirectory());
        String scratchClassDirPath = scratchClassDir.getAbsolutePath();

        // determine current class loader
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = this.getClass().getClassLoader();
        }

        // configure URI resolvers
        File xpSourceDirectoryFile = new File(getXpSourceDirectory());
        UnifiedResolver resolver = new UnifiedResolver();
        resolver.setDefaultLookupEnabled(false);
        resolver.addProtocolHandler("classpath",
                new ClassLoaderURIHandler(loader));
        resolver.addProtocolHandler("file",
                new URLChangeRootURIHandler(xpSourceDirectoryFile.toURL()));

        // determine xpRegistry configuration file
        String xpRegistry = getXpRegistryFile();

        // configure the XpCachingLoader
        cache = XpCachingLoader.getLoader();
        cache.setXpRegistry(xpRegistry);
        cache.setResolver(resolver);
        cache.setJavaRoot(scratchJavaDirPath);
        cache.setClassRoot(scratchClassDirPath);
        cache.setParentLoader(loader);

    }

    public void service(XpContext xpContext, URI xpURI) throws Exception {

        XpRunner xpRunner = cache.getXpRunner(xpURI);
        if (xpRunner == null) {
            throw new Exception("could not get xpRunner for: " + xpURI);
        }

        xpRunner.setEncoding("UTF-8");
        OutputStream out = System.out;
        xpRunner.run(xpContext, out);

    }

    public String getXpSourceDirectory() { return xpSourceDirectory; }
    public void setXpSourceDirectory(String xpSourceDirectory) { this.xpSourceDirectory = xpSourceDirectory; }

    public String getScratchJavaDirectory() { return scratchJavaDirectory; }
    public void setScratchJavaDirectory(String scratchDirectory) { this.scratchJavaDirectory = scratchDirectory; }

    public String getXpRegistryFile() { return xpRegistryFile; }
    public void setXpRegistryFile(String xpRegistryFile) { this.xpRegistryFile = xpRegistryFile; }

    public String getScratchClassDirectory() { return scratchClassDirectory; }
    public void setScratchClassDirectory(String scratchClassDirectory) { this.scratchClassDirectory = scratchClassDirectory; }

}
