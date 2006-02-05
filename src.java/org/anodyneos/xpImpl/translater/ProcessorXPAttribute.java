package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Attribute element must have name and either expr attribute or text contents.
 *
 * This translater supports two modes:
 *
 * 1. No body mode if value attribute exists.  Value of attribute is provided as an attribute of xp:attribute.
 *
 * 2. Optimized mode if body contains only text.  Sets the attribute to the value of the content, or the value of the content
 * after EL evaluations.
 *
 * 3. If body contains output elements and/or tags, processes the content using <code>ProcessorResultContent</code> to
 * allow for runtime branching, etc.  Sets attribute to runtime string result.
 */
class ProcessorXPAttribute extends ProcessorForType {

    public static final String A_NAME = "name";
    public static final String A_NAMESPACE = "namespace";
    public static final String A_VALUE = "value";
    public static final String A_TRIM = "trim";

    private boolean valueAttributeMode = true;

    private String codeName;
    private String codeURI;
    private boolean trim = true;

    public ProcessorXPAttribute(TranslaterContext ctx) throws SAXException {
        super(ctx, "String");
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        if (valueAttributeMode) {
            throw new SAXParseException("Element not allowed here: <" + qName + "> when @value is present on <xp:attribute>;", getContext().getLocator());
        }
        return super.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        CodeWriter out = getTranslaterContext().getCodeWriter();
        String value = attributes.getValue(A_VALUE);
        String attName = attributes.getValue(A_NAME);
        String attURI = attributes.getValue(A_NAMESPACE);
        String attTrim = attributes.getValue(A_TRIM);

        if(null != attTrim) {
            trim = Boolean.parseBoolean(attTrim);
        }

        if(Util.hasEL(attName)) {
            // EL expression may exist.  Process all unescaped expressions, concatinate, etc...
            codeName = Util.elExpressionCode(attName, "String");
        } else {
            codeName = Util.escapeStringQuotedEL(attName);
        }

        if (null == attURI) {
            codeURI = "null";
        } else if(Util.hasEL(attURI)) {
            // EL expression may exist.  Process all unescaped expressions, concatinate, etc...
            codeURI = Util.elExpressionCode(attURI, "String");
        } else {
            codeURI = Util.escapeStringQuotedEL(attURI);
        }

        if (null == value) {
            valueAttributeMode = false;
        } else {
            valueAttributeMode = true;
            String codeValue;
            if(Util.hasEL(value)) {
                // EL expression may exist.  Process all unescaped expressions, concatinate, etc...
                codeValue = Util.elExpressionCode(value, "String");
                if (trim) {
                    codeValue = "(" + codeValue + ").trim()";
                }
            } else {
                if (trim) {
                    codeValue = Util.escapeStringQuotedEL(value.trim());
                } else {
                    codeValue = Util.escapeStringQuotedEL(value);
                }
            }
            out.printIndent().println(
                  "xpCH.addAttribute("
                +       codeURI
                + "," + codeName
                + "," + codeValue
                + ");"
            );
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (valueAttributeMode) {
            return;
        }
        super.endElement(uri, localName, qName);
    }


    public void process(String xpOutVar, String expr) {

        CodeWriter out = getTranslaterContext().getCodeWriter();

        if (trim) {
            expr = "(" + expr + ").trim()";
        }

        out.printIndent().println(
              xpOutVar + ".addAttribute("
            +       codeURI
            + "," + codeName
            + "," + expr
            + ");"
        );
    }
}
