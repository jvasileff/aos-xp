package org.anodyneos.xpImpl.compiler;

import java.io.File;
import java.io.FileFilter;

public class FileExtensionFilter implements FileFilter {

    private String suffix;

    public FileExtensionFilter(String extension) {
        this.suffix = "." + extension;
    }

    public boolean accept(File file) {
        //System.out.println(file);
        return (file.isDirectory() || file.getName().endsWith(suffix));
    }
}
