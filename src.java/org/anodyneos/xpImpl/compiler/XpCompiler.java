package org.anodyneos.xpImpl.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.anodyneos.xpImpl.translater.Translater;
import org.anodyneos.xpImpl.translater.TranslaterResult;
import org.xml.sax.InputSource;

public class XpCompiler {

    public void compile(XpCompilerContext xpcCtx, XpFile[] xpFiles) throws Exception {
        Translater translater = new Translater();
        ArrayList javaFiles = new ArrayList();

        // translate
        for(int i = 0; i < xpFiles.length; i++) {
            // create SAX InputSource
            InputStream is = new FileInputStream(xpFiles[i].getXpSourceFile());
            InputSource inputSource = new InputSource(is);
            inputSource.setSystemId(xpFiles[i].getXpSourceFile().getAbsolutePath());

            // write to a temp file since we don't know what the class name
            // will be yet (we actually do know if xpFile.getFullClassName()
            // does not return null.)
            File tmpFile = File.createTempFile("xpc.", ".tmp", xpcCtx.getJavaDirectory());
            OutputStream os = new FileOutputStream(tmpFile);
            TranslaterResult tr = translater.process(inputSource, os, xpcCtx.getTagLibraryRegistry(), xpFiles[i].getFullClassName());
            is.close();
            os.close();

            // rename tmpFile to follow conventions
            // TODO: put in subdirectory to handle name conflicts across packages.
            File javaFile = new File(xpcCtx.getJavaDirectory(), tr.getClassName() + ".java");
            tmpFile.renameTo(javaFile);
            javaFiles.add(javaFile.getAbsolutePath());
        }

        // java compile
        xpcCtx.getJavaCompiler().compile((String[]) javaFiles.toArray(new String[javaFiles.size()]), xpcCtx.getOut());
    }

}
