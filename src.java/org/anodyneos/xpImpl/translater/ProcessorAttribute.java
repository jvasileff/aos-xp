package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Attribute element must have name and either expr attribute or text contents.
 * TODO: add support for attribute value being the CDATA content of this element.
 */
class ProcessorAttribute extends TranslaterProcessor {

    public static final String A_NAME = "name";
    public static final String A_NAMESPACE = "namespace";
    public static final String A_VALUE = "value";

    private String attName;
    private String attURI;
    private String attCodeValue;

    public ProcessorAttribute(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return super.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        String value = attributes.getValue(A_VALUE);
        attName = attributes.getValue(A_NAME);
        attURI = attributes.getValue(A_NAMESPACE);

        if(null == attURI) {
            attURI = "";
        }

        if(Util.hasEL(value)) {
            // EL expression may exist.  Process all unescaped expressions, concatinate, etc...
            attCodeValue = Util.elExpressionCode(value, "String");
        } else {
            attCodeValue = Util.escapeStringQuotedEL(value);
        }
    }

    //public void characters(char[] ch, int start, int length) {
        // collect contents
        //sb.append(ch, start, length);
    //}

    public void endElement(String uri, String localName, String qName) {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        //addManagedAttribute(String name, String namespaceURI, String value)
        out.printIndent().println(
              "xpCH.addManagedAttribute("
            +       Util.escapeStringQuoted(attName)
            + "," + Util.escapeStringQuoted(attURI)
            + "," + attCodeValue
            + ");"
        );
    }
}
