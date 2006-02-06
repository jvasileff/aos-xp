package org.anodyneos.xpImpl.util;

import java.util.ArrayList;
import java.util.Iterator;

public class JavaClass {

    private String className;
    private String packageName;
    private String classExtends;
    private ArrayList<String> imports = new ArrayList<String>();
    private ArrayList<String> interfaces = new ArrayList<String>();

    // constructors
    public JavaClass() {
    }

    // setters
    public void setExtends(String s) {
        this.classExtends = s;
    }
    public void addImport(String s) {
        this.imports.add(s);
    }
    public void addInterface(String s) {
        this.interfaces.add(s);
    }

    // getters
    public void setFullClassName(String fullClassName) {
        if (null == fullClassName) {
            this.packageName = null;
            this.className = null;
        } else {
            fullClassName = fullClassName.trim();
            int lastDot = fullClassName.lastIndexOf(".");
            if (lastDot != -1) {
                this.packageName = fullClassName.substring(0, lastDot);
                this.className = fullClassName.substring(lastDot + 1);
            } else {
                this.packageName = null;
                this.className = fullClassName;
            }
        }
    }
    public String getFullClassName() {
        if(packageName != null) {
            return packageName + "." + className;
        } else {
            return className;
        }
    }

    public void setPackageName(String packageName) {
        packageName = packageName.trim();
        this.packageName = packageName;
    }
    public String getPackageName() {
        return this.packageName;
    }
    public void setClassName(String className) {
        className = className.trim();
        this.className = className;
    }
    public String getClassName() {
        return this.className;
    }

    // printers
    public void printHeader(CodeWriter out) {
        printPackage(out);
        printImports(out);
        printClassDeclaration(out);
    }
    public void printPackage(CodeWriter out) {
        if (null != packageName) {
            out.printIndent().println("package " + packageName + ";");
            out.println();
        }
    }
    public void printImports(CodeWriter out) {
        Iterator it = imports.iterator();
        boolean hasAny = false;
        while (it.hasNext()) {
            hasAny = true;
            out.printIndent().println("import "
                    + it.next()
                    + ";");
        }
        if (hasAny) out.println();
    }
    public void printClassDeclaration(CodeWriter out) {
        out.printIndent().print("public class " + className);
        if(null != classExtends) {
            out.print(" extends " + classExtends);
        }
        if(interfaces.size() > 0) {
            out.print(" implements ");
            for (int i = 0; i < interfaces.size(); i++) {
                if(i != 0) {
                    out.print(", ");
                }
                out.print(interfaces.get(i));
            }
        }
        out.println(" {");
        out.println();
        out.indentPlus();
    }
}
