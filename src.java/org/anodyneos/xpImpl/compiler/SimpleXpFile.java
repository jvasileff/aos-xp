package org.anodyneos.xpImpl.compiler;

import java.io.File;

public class SimpleXpFile implements XpFile {
    private File sourceFile;
    private String fullClassName;

    public SimpleXpFile(File sourceFile, String fullClassName) {
        this.sourceFile = sourceFile;
        this.fullClassName = fullClassName;
    }

    public File getXpSourceFile() {
        return sourceFile;
    }
    public String getFullClassName() {
        return fullClassName;
    }

}
