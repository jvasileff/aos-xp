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
    public static final String A_NAMESPACE = "namespace";
    public static final String A_VALUE = "value";

    String attLocalName;
    String attQName;
    String attURI;
    String attPrefix = null;
    boolean declareNamespace = false;
    String attCodeValue;

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
        String namespace = attributes.getValue(A_NAMESPACE);
        String value = attributes.getValue(A_VALUE);

        if(null != namespace && namespace.length()==0) {
            namespace = null;
        }

        if(name.indexOf(':') == -1) {
            if (null != namespace) {
                // no prefix provided but namespace specified
                attLocalName = name;
                // TODO: choose a prefix that isn't being used.  Need to have XPContentHandler track prefixes.
                attPrefix = "ns0";
                attQName = attPrefix + ":" + name;
                attURI = namespace;
                declareNamespace = true;
            } else {
                // no prefix or namespace
                attLocalName = name;
                attQName = name;
                attURI = "";
            }
        } else {
            if (null != namespace) {
                // prefix provided and namespace specified
                attPrefix = name.substring(0, name.indexOf(":"));
                attLocalName = name.substring(name.indexOf(":") + 1);
                attQName = name;
                attURI = namespace;
                declareNamespace = true;
            } else {
                // prefix provided and no namespace specified
                // TODO: do we need validation on the namespace here or does the consumer
                // of our SAX messages perform checks?
                attLocalName = name;
                attQName = name;
                attURI = "";
            }
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

    //public void endElement(String uri, String localName, String qName) {
    //}
}
