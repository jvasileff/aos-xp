package org.anodyneos.xpImpl.compiler;

import java.io.OutputStream;
import java.util.ArrayList;

@SuppressWarnings({"deprecation"})
public class SunJavaCompiler extends JavaCompiler {

    public SunJavaCompiler() {
    }

    public SunJavaCompiler(String classpath, String outputDirectory) {
        super(classpath, outputDirectory);
    }


    public boolean compile(String[] files, OutputStream out) {
        sun.tools.javac.Main compiler = new sun.tools.javac.Main(out, "");
        String[] args = getArgs(files);

        return compiler.compile(args);
    }

    public String[] getArgs(String[] files) {
        ArrayList<String> args = new ArrayList<String>();

        // debug
        if(debugSource || debugLines || debugLocalVariables) {
            StringBuffer arg = new StringBuffer("-g:");
            boolean first = true;
            if(debugSource) {
                if(!first) { arg.append(","); first = false; }
                arg.append("source");
            }
            if(debugLines) {
                if(!first) { arg.append(","); first = false; }
                arg.append("lines");
            }
            if(debugLocalVariables) {
                if(!first) { arg.append(","); first = false; }
                arg.append("variables");
            }
            args.add(arg.toString());
        } else {
            args.add("-g:none");
        }

        // optimize
        if(optimize) {
            args.add("-O");
        }

        // classPath
        if(null != classPath) {
            args.add("-classpath");
            args.add(classPath);
        }

        // sourcePath
        if(null != sourcePath) {
            args.add("-sourcepath");
            args.add(sourcePath);
        }

        // bootClassPath
        if(null != bootClassPath) {
            args.add("-bootclasspath");
            args.add(bootClassPath);
        }

        // extDirs
        if(null != extDirs) {
            args.add("-extdirs");
            args.add(extDirs);
        }

        // outputDirectory
        if(outputDirectory != null) {
            args.add("-d");
            args.add(outputDirectory);
        }

        // encoding
        if(encoding != null) {
            args.add("-encoding");
            args.add(encoding);
        }

        // target
        if(target == TARGET_JAVA_1_1) {
            args.add("-target");
            args.add("1.1");
        }
        if(target == TARGET_JAVA_1_2) {
            args.add("-target");
            args.add("1.2");
        }
        if(target == TARGET_JAVA_1_3) {
            args.add("-target");
            args.add("1.3");
        }

        for (int i = 0; i < files.length; i++) {
            args.add(files[i]);
        }

        return args.toArray(new String[args.size()]);
    }

}
