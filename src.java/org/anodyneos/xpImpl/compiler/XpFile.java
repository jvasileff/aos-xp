package org.anodyneos.xpImpl.compiler;

import java.io.File;

/**
 *  Instances of XpFile can be passed to XpCompiler for translation and compilation.
 */
public interface XpFile {

    File getXpSourceFile();

    /**
     *  Returns the full class name (package and class) to be used for this
     *  XpFile if specified.  If null is returned, the full class name should
     *  be retrieved from the contents of the XpSourceFile.
     */
    String getFullClassName();

}
