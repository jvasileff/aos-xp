package org.anodyneos.xpImpl.util;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

public class CodeWriter extends PrintWriter {

    public static final int DEFAULT_INDENT_AMOUNT = 4;

    private int indentLevel = 0;
    private int indentAmount = DEFAULT_INDENT_AMOUNT;

    // constructors
    public CodeWriter(PrintWriter out) {
        super(out);
    }
    public CodeWriter(OutputStream out) {
        super(out);
    }
    public CodeWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }
    public CodeWriter(Writer out) {
        super(out);
    }
    public CodeWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    // indent
    public CodeWriter indentPlus() {
        indentLevel += 1;
        return this;
    }
    public CodeWriter indentMinus() {
        if (indentLevel > 0)  indentLevel -= 1;
        return this;
    }
    public CodeWriter printIndent() {
        for(int i = 0; i < indentLevel * indentAmount; i++) {
            print(' ');
        }
        return this;
    }

    // util
    public CodeWriter endBlock() {
        indentMinus();
        printIndent().println("}");
        return this;
    }

}
