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
public class ProcessorXPTagWhen extends HelperProcessorNonResultContent {

    ProcessorResultContent processorResultContent;

    public static final String A_TEST = "test";
    private boolean isFirst;
    private boolean isOtherwise;

    public ProcessorXPTagWhen(TranslaterContext ctx, boolean isOtherwise, boolean isFirst) {
        super(ctx);
        processorResultContent = new ProcessorResultContent(ctx);
        this.isOtherwise = isOtherwise;
        this.isFirst = isFirst;
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return processorResultContent.getProcessorFor(uri, localName, qName);
    }

    public void startElementNonResultContent(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        if (! isOtherwise) {
            String test = attributes.getValue(A_TEST);

            if(null == test) {
                throw new SAXParseException("@test is required.", getContext().getLocator());
            }

            // codeTest will be a boolean with or without EL.
            String codeTest = Util.elExpressionCode(test, "boolean");

            if (isFirst) {
                out.printIndent().println("if (" + codeTest + ") {");
                out.indentPlus();
            } else {
                out.indentMinus();
                out.printIndent().println("} else if (" + codeTest + ") {");
                out.indentPlus();
            }
        } else {
            if (isFirst) {
                out.printIndent().println("if (true) {");
                out.indentPlus();
            } else {
                out.indentMinus();
                out.printIndent().println("} else {");
                out.indentPlus();
            }
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        processorResultContent.characters(ch, start, length);
    }

    public void endElementNonResultContent(String uri, String localName, String qName) throws SAXException {
        processorResultContent.flushCharacters();
    }

}
