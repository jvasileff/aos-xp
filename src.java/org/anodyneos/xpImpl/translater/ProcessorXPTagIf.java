package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * @author jvas
 */
public class ProcessorXPTagIf extends TranslaterProcessor {

    ProcessorResultContent processorResultContent;

    public static final String A_TEST = "test";
    public static final String A_VAR = "var";
    public static final String A_SCOPE = "scope";

    public ProcessorXPTagIf(TranslaterContext ctx) {
        super(ctx);
        processorResultContent = new ProcessorResultContent(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return processorResultContent.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        String test = attributes.getValue(A_TEST);
        String var = attributes.getValue(A_VAR);
        String scope = attributes.getValue(A_SCOPE);

        if(null == test) {
            throw new SAXParseException("@test is required.", getContext().getLocator());
        }

        if(null != scope && null == var) {
            throw new SAXParseException("@var must be specified when @scope is specified.", getContext().getLocator());
        }

        // codeTest will be a boolean with or without EL.
        String codeTest = Util.elExpressionCode(test, "boolean");

        if (null != var && null != scope) {
            out.printIndent().println("if (!(" + codeTest + ")) {");
            out.indentPlus();
            out.printIndent().println(
                      "xpContext.setAttribute("
                    + Util.escapeStringQuotedEL(var)
                    + ", Boolean.FALSE"
                    + ", xpContext.resolveScope(" + Util.escapeStringQuotedEL(scope) + "));"
            );
            out.indentMinus();
            out.printIndent().println("} else {");
            out.indentPlus();
            out.printIndent().println(
                      "xpContext.setAttribute("
                    + Util.escapeStringQuotedEL(var)
                    + ", Boolean.TRUE"
                    + ", xpContext.resolveScope(" + Util.escapeStringQuotedEL(scope) + "));"
            );
        } else if (null != var && null == scope) {
            out.printIndent().println("if (!(" + codeTest + ")) {");
            out.indentPlus();
            out.printIndent().println(
                      "xpContext.setAttribute("
                    + Util.escapeStringQuotedEL(var)
                    + ", Boolean.FALSE);"
            );
            out.indentMinus();
            out.printIndent().println("} else {");
            out.indentPlus();
            out.printIndent().println(
                      "xpContext.setAttribute("
                    + Util.escapeStringQuotedEL(var)
                    + ", Boolean.TRUE);"
            );
        } else {
            out.printIndent().println("if (" + codeTest + ") {");
            out.indentPlus();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        processorResultContent.characters(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        processorResultContent.flushCharacters();
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.endBlock();
    }

}
