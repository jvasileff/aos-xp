package org.anodyneos.xpImpl.compiler;

import java.io.File;
import java.io.OutputStream;

import org.anodyneos.xp.tagext.TagLibraryRegistry;


public class SimpleXpCompilerContext implements XpCompilerContext {

    private JavaCompiler javaCompiler;
    private TagLibraryRegistry tagLibraryRegistry;
    private File javaDirectory;
    private OutputStream out;

    public SimpleXpCompilerContext( JavaCompiler javaCompiler,
            TagLibraryRegistry tagLibraryRegistry, File javaDirectory, OutputStream out) {
        this.javaCompiler = javaCompiler;
        this.tagLibraryRegistry = tagLibraryRegistry;
        this.javaDirectory = javaDirectory;
        this.out = out;
    }

    public JavaCompiler getJavaCompiler() {
        return javaCompiler;
    }
    public TagLibraryRegistry getTagLibraryRegistry() {
        return tagLibraryRegistry;
    }
    public File getJavaDirectory() {
        return javaDirectory;
    }
    public OutputStream getOut() {
        return out;
    }

}
