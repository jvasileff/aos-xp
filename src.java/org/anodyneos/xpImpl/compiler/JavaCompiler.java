package org.anodyneos.xpImpl.compiler;

import java.io.OutputStream;

public abstract class JavaCompiler {

    public static final int TARGET_DEFAULT = 0;
    public static final int TARGET_JAVA_1_1 = 1;
    public static final int TARGET_JAVA_1_2 = 2;
    public static final int TARGET_JAVA_1_3 = 3;

    protected boolean debugSource = false;
    protected boolean debugLines = true;
    protected boolean debugLocalVariables = false;

    protected boolean optimize = false;
    protected String classPath = null;
    protected String sourcePath = null;
    protected String bootClassPath = null;
    protected String extDirs = null;

    protected String outputDirectory = null;
    protected String encoding = null;
    protected int target = TARGET_DEFAULT;

    public JavaCompiler() {
    }
    public JavaCompiler(String classpath, String outputDirectory) {
        setClassPath(classpath);
        setOutputDirectory(outputDirectory);
    }

    public void setDebugSource(boolean val) {
        this.debugSource = val;
    }
    public boolean getDebugSource() {
        return this.debugSource;
    }

    public void setDebugLines(boolean val) {
        this.debugLines = val;
    }
    public boolean getDebugLines() {
        return this.debugLines;
    }

    public void setDebugLocalVariables(boolean val) {
        this.debugLocalVariables = val;
    }
    public boolean getDebugVariables() {
        return this.debugLocalVariables;
    }

    public void setOptimize(boolean val) {
        this.optimize = val;
    }
    public boolean getOptimize() {
        return this.optimize;
    }

    public void setClassPath(String val) {
        this.classPath = val;
    }
    public String getClassPath() {
        return classPath;
    }

    public void setSourcePath(String val) {
        this.sourcePath = val;
    }
    public String getSourcePath() {
        return sourcePath;
    }

    public void setBootClassPath(String val) {
        this.bootClassPath = val;
    }
    public String getBootClassPath() {
        return bootClassPath;
    }

    public void setExtDirs(String val) {
        this.extDirs = val;
    }
    public String getExtDirs() {
        return extDirs;
    }

    public void setOutputDirectory(String val) {
        this.outputDirectory = val;
    }
    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setEncoding(String val) {
        this.encoding = val;
    }
    public String getEncoding() {
        return encoding;
    }

    public void setTarget(int val) {
        this.target = val;
    }
    public int getTarget() {
        return target;
    }

    public boolean compile(String file, OutputStream out) {
        return compile(new String[] {file}, out);
    }

    public abstract boolean compile(String[] files, OutputStream out);

}
