package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * @author yao
 */
public class ProcessorXPTagInclude extends TranslaterProcessor {

    ProcessorResultContent processorResultContent;

    public static final String A_FILE = "file";

    public ProcessorXPTagInclude(TranslaterContext ctx) {
        super(ctx);
        processorResultContent = new ProcessorResultContent(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return processorResultContent.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        String file = attributes.getValue(A_FILE);

        if(null == file) {
            throw new SAXParseException("@file is required.", getContext().getLocator());
        }

        out.printIndent().println("new " +
                TranslaterContext.DEFAULT_PACKAGE + "." + file.replace('/','.') + "().service(xpContext,xpCH);");


    }


    public void characters(char[] ch, int start, int length) throws SAXException {
        processorResultContent.characters(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        processorResultContent.flushCharacters();
    }

}
