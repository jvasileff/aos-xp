package org.anodyneos.xpImpl.compiler;

import java.io.File;
import java.io.FileFilter;

public class JavaArchiveFilter implements FileFilter {

    public boolean accept(File file) {
        String name = file.getName().toLowerCase();
        return (name.endsWith(".jar") || name.endsWith(".zip"));
    }

}

