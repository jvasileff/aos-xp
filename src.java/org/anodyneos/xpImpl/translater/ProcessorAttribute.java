package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Attribute element must have name and either expr attribute or text contents.
 * @TODO: add support for attribute value being the CDATA content of this element.
 * @TODO: add support for namespace
 */
class ProcessorAttribute extends TranslaterProcessor {

    public static final String A_NAME = "name";
    public static final String A_VALUE = "value";

    public ProcessorAttribute(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return super.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        String name = attributes.getValue(A_NAME);
        String value = attributes.getValue(A_VALUE);
        String codeValue;
        //if(value.indexOf("${") != -1) {
            // EL expression may exist.  Process all unescaped expressions, concatinate, etc...
        //} else {
            codeValue = Util.escapeStringQuoted(value);
        //}
        out.printIndent().println(
                  "xpContentHandler.addAttribute("
                +        "\"\"" // URI
                + ", " + Util.escapeStringQuoted(name) // localName
                + ", " + Util.escapeStringQuoted(name) // qName
                + ", " + "\"CDATA\"" // type
                + ", " + codeValue
                + ");"
        );
    }

    //public void characters(char[] ch, int start, int length) {
        // collect contents
        //sb.append(ch, start, length);
    //}

    //public void endElement(String uri, String localName, String qName) {
    //}
}
