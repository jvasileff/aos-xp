package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.util.HashMap;

/**
 * @author yao
 */
public class ProcessorXPTagInclude extends TranslaterProcessor {

    ProcessorResultContent processorResultContent;
    public static final String E_PARAM = "param";
    public static final String A_FILE = "file";

    public ProcessorXPTagInclude(TranslaterContext ctx) {
        super(ctx);
        processorResultContent = new ProcessorResultContent(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {

        if (URI_XP.equals(uri) && E_PARAM.equals(localName)) {
            // TODO not yet implemented
            //ElementProcessor proc = new ProcessorXPTagIncludeParam(getTranslaterContext(), varName);
            //return proc;
            throw new SAXParseException(localName + " has not been implemented yet.", getContext().getLocator());
        } else {
            throw new SAXParseException(localName + " is not valid inside <include>.", getContext().getLocator());
        }
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        String file = attributes.getValue(A_FILE);

        if(null == file) {
            throw new SAXParseException("@file is a required attribute for <include>", getContext().getLocator());
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
