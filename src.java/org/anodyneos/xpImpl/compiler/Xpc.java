package org.anodyneos.xpImpl.compiler;

import java.io.File;
import java.util.StringTokenizer;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.xp.tagext.TagLibraryRegistry;
import org.anodyneos.xpImpl.registry.RegistryParser;
import org.xml.sax.InputSource;

public class Xpc {

    public static void main(String[] args) throws Exception {
        // registry
        UnifiedResolver resolver = new UnifiedResolver();
        resolver.addProtocolHandler("classpath",
                new ClassLoaderURIHandler(RegistryParser.class.getClassLoader()));
        InputSource is = new InputSource(new java.io.File(args[3]).toURL().toString());
        TagLibraryRegistry registry = new RegistryParser().process(is, resolver);

        // xpcCtx
        XpCompilerContext xpcCtx = new SimpleXpCompilerContext(
                new SunJavaCompiler(generateClassPath(), args[2]),
                registry,
                new File(args[1]),      // javaDir
                System.out);

        // xpCompiler
        XpCompiler xpCompiler = new XpCompiler();

        // xpFiles
        XpFile[] xpFiles = getXpFiles(new File(args[0]));

        // DOIT
        xpCompiler.compile(xpcCtx, xpFiles);
    }

    public static XpFile[] getXpFiles(File xpRootDir) throws Exception {
        File[] files =
                FileLister.deepListFiles(
                    xpRootDir, new FileExtensionFilter("xp"));

        XpFile[] xpFiles = new SimpleXpFile[files.length];
        for(int i = 0; i < files.length; i++) {
            File file = files[i];
            xpFiles[i] = new SimpleXpFile(
                    files[i],
                    "xp.runtime." + classNameFor(xpRootDir, file));
        }
        return xpFiles;
    }

    private static String classNameFor(File rootDir, File xpFile) {
        String relativePart = xpFile.getAbsolutePath().substring(
                rootDir.getAbsolutePath().length() + 1);
        relativePart = relativePart.replace(File.separatorChar, '_');
        // turn "." to "_"
        return(relativePart.substring(0, relativePart.length() - 3));
    }


    /* ****** from jasper compiler/JspRuntimeContext.java

         * Method used to initialize classpath for compiles.

        private void initClassPath() {

            URL [] urls = parentClassLoader.getURLs();
            StringBuffer cpath = new StringBuffer();
            String sep = System.getProperty("path.separator");

            for(int i = 0; i < urls.length; i++) {
                // Tomcat 4 can use URL's other than file URL's,
                // a protocol other than file: will generate a
                // bad file system path, so only add file:
                // protocol URL's to the classpath.
                if( urls[i].getProtocol().equals("file") ) {
                    cpath.append((String)urls[i].getFile()+sep);
                }
            }

            String cp = (String) context.getAttribute(Constants.SERVLET_CLASSPATH);
            if (cp == null || cp.equals("")) {
                cp = options.getClassPath();
            }

            classpath = cpath.toString() + cp;
        }
    */

    /* ****** from cocoon  JavaLanguage.java
        public void initialize() throws Exception {

            // Initialize the classpath
            String systemBootClasspath = System.getProperty("sun.boot.class.path");
            String systemClasspath = System.getProperty("java.class.path");
            String systemExtDirs = System.getProperty("java.ext.dirs");
            String systemExtClasspath = null;

            try {
                systemExtClasspath = expandDirs(systemExtDirs);
            } catch (Exception e) {
                getLogger().warn("Could not expand Directory:" + systemExtDirs, e);
            }

            this.classpath =
                ((super.classpath != null) ? File.pathSeparator + super.classpath : "") +
                ((systemBootClasspath != null) ? File.pathSeparator + systemBootClasspath : "") +
                ((systemClasspath != null) ? File.pathSeparator + systemClasspath : "") +
                ((systemExtClasspath != null) ? File.pathSeparator + systemExtClasspath : "");
        }
    */
    /* ****** from cocoon CompiledProgrammingLanguage.java
        public void contextualize(Context context) throws ContextException {
            this.classpath = (String) context.get(Constants.CONTEXT_CLASSPATH);
        }
    */
    /* ****** from cocoon CocoonServlet.java (** read file for more)
         protected String getClassPath() throws ServletException {
            StringBuffer buildClassPath = new StringBuffer();

            File root = null;
            if (servletContextPath != null) {
                // Old method.  There *MUST* be a better method than this...

                String classDir = this.servletContext.getRealPath("/WEB-INF/classes");
                String libDir = this.servletContext.getRealPath("/WEB-INF/lib");

                if (libDir != null) {
                    root = new File(libDir);
                }

                if (classDir != null) {
                    buildClassPath.append(classDir);

                    addClassLoaderDirectory(classDir);
                }
            } else {
                // New(ish) method for war'd deployments
                URL classDirURL = null;
        ...
    */

    private static String generateClassPath() {
        // Initialize the classpath
        String systemBootClasspath = System.getProperty("sun.boot.class.path");
        String systemClasspath = System.getProperty("java.class.path");
        String systemExtDirs = System.getProperty("java.ext.dirs");
        String systemExtClasspath = null;

        try {
            systemExtClasspath = expandDirs(systemExtDirs);
        } catch (Exception e) {
            //getLogger().warn("Could not expand Directory:" + systemExtDirs, e);
        }

        return
            ((systemBootClasspath != null) ? File.pathSeparator + systemBootClasspath : "") +
            ((systemClasspath != null) ? File.pathSeparator + systemClasspath : "") +
            ((systemExtClasspath != null) ? File.pathSeparator + systemExtClasspath : "");
    }

    /**
     * Expand a directory path or list of directory paths (File.pathSeparator
     * delimited) into a list of file paths of all the jar files in those
     * directories.
     *
     * @param dirPaths The string containing the directory path or list of
     *          directory paths.
     * @return The file paths of the jar files in the directories. This is an
     *          empty string if no files were found, and is terminated by an
     *          additional pathSeparator in all other cases.
     */
    private static String expandDirs(String dirPaths) {
        StringTokenizer st = new StringTokenizer(dirPaths, File.pathSeparator);
        StringBuffer buffer = new StringBuffer();
        while (st.hasMoreTokens()) {
            String d = st.nextToken();
            File dir = new File(d);
            if (!dir.isDirectory()) {
                // The absence of a listed directory may not be an error.
                //if (getLogger().isWarnEnabled()) getLogger().warn("Attempted to retrieve directory listing of non-directory " + dir.toString());
            } else {
                File[] files = dir.listFiles(new JavaArchiveFilter());
                for (int i = 0; i < files.length; i++) {
                    buffer.append(files[i]).append(File.pathSeparator);
                }
            }
        }
        return buffer.toString();
    }

}
