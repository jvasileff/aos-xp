package test.org.anodyneos.xpImpl;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.net.URLChangeRootURIHandler;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpFactory;
import org.anodyneos.xp.XpPage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XpStandaloneTest {

    private static final Log log = LogFactory.getLog(XpStandaloneTest.class);

    private static final long serialVersionUID = 1L;

    private XpFactory xpFactory;
    private URI xpRegistryURI;
    private File xpSourceDirectory;
    private File scratchJavaDirectory;
    private File scratchClassDirectory;

    public static void main(String[] args) throws Exception {
        XpStandaloneTest obj = new XpStandaloneTest();
        obj.setScratchJavaDirectory(new File(args[0]));
        obj.setScratchClassDirectory(new File(args[1]));
        obj.setXpSourceDirectory(new File(args[2]));
        obj.setXpRegistryURI(new URI(args[3]));
        obj.init();

        obj.service(new URI(args[4]));
    }

    public void init() throws Exception {

        // validate scratch directories
        File scratchJavaDir = getScratchJavaDirectory();
        if (!(scratchJavaDir.exists() && scratchJavaDir.canRead() && scratchJavaDir.canWrite())) {
            throw new Exception("Java work directory is invalid.  Check for existance and Read/Write settings: " + scratchJavaDir);
        }

        File scratchClassDir = getScratchClassDirectory();
        if (!(scratchClassDir.exists() && scratchClassDir.canRead() && scratchClassDir.canWrite())) {
            throw new Exception("Class work directory is invalid.  Check for existance and Read/Write settings: " + scratchClassDir);
        }

        // configure URI resolvers
        File xpSourceDirectoryFile = getXpSourceDirectory();
        UnifiedResolver resolver = new UnifiedResolver();
        resolver.setDefaultLookupEnabled(false);
        resolver.addProtocolHandler("classpath", new ClassLoaderURIHandler());
        resolver.addProtocolHandler("file", new URLChangeRootURIHandler(xpSourceDirectoryFile.toURI().toURL()));

        // configure the XpCachingLoader
        xpFactory = XpFactory.newInstance();
        xpFactory.setXpRegistryURI(getXpRegistryURI());
        xpFactory.setResolver(resolver);
        xpFactory.setJavaGenDirectory(getScratchJavaDirectory());
        xpFactory.setClassGenDirectory(getScratchClassDirectory());

    }

    public void service(URI xpURI) throws Exception {

        XpContext ctx = xpFactory.getStandaloneXpContext();
        XpPage xpPage = xpFactory.newXpPage(xpURI);
        if (xpPage == null) {
            throw new Exception("could not get XpPage for: " + xpURI);
        }

        xpPage.setEncoding("UTF-8");
        OutputStream out = System.out;
        xpPage.service(ctx, out);

    }

    public File getXpSourceDirectory() { return xpSourceDirectory; }
    public void setXpSourceDirectory(File xpSourceDirectory) { this.xpSourceDirectory = xpSourceDirectory; }

    public File getScratchJavaDirectory() { return scratchJavaDirectory; }
    public void setScratchJavaDirectory(File scratchDirectory) { this.scratchJavaDirectory = scratchDirectory; }

    public URI getXpRegistryURI() { return xpRegistryURI; }
    public void setXpRegistryURI(URI xpRegistryURI) { this.xpRegistryURI = xpRegistryURI; }

    public File getScratchClassDirectory() { return scratchClassDirectory; }
    public void setScratchClassDirectory(File scratchClassDirectory) { this.scratchClassDirectory = scratchClassDirectory; }

}
