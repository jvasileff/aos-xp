package org.anodyneos.xpImpl.compiler;

import java.io.File;
import java.io.OutputStream;

import org.anodyneos.xp.tagext.TagLibraryRegistry;


/**
 *  Provides necessary context for translating and compiling XP source files.
 *  Subclasses may support advanced features such as registry reloading to
 *  allow dynamic updating without restarting the server.
 */
public interface XpCompilerContext {

    JavaCompiler getJavaCompiler();
    TagLibraryRegistry getTagLibraryRegistry();
    File getJavaDirectory();
    OutputStream getOut();

}
