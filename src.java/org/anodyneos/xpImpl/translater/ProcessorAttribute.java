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
class ProcessorAttribute extends TranslaterProcessor {

    public static final String A_NAME = "name";
    public static final String A_NAMESPACE = "namespace";
    public static final String A_VALUE = "value";
    public static final String A_TRIM = "trim";

    ProcessorResultContent processorResultContent;
    private boolean valueAttributeMode = true;
    private boolean optimizedMode = true;
    private String savedXPOutVariable;
    private StringBuffer sb;

    private String codeName;
    private String codeURI;
    private boolean trim = true;

    public ProcessorAttribute(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        if (valueAttributeMode) {
            throw new SAXParseException("Element not allowed here: <" + qName + "> when @value is present on <xp:attribute>;", getContext().getLocator());
        }
        // new element is coming, so we cannot run in optimized mode.
        if (optimizedMode) {
            // switch to non-optimized mode
            optimizedMode = false;
            CodeWriter out = getTranslaterContext().getCodeWriter();
            savedXPOutVariable = getTranslaterContext().getVariableForSavedXPOut();
            out.printIndent().println( "org.anodyneos.xp.XpOutput " + savedXPOutVariable + " = xpOut;");
            out.printIndent().println( "xpOut = new org.anodyneos.xp.XpOutput(new org.anodyneos.xp.util.TextContentHandler(), xpCH);" );
            out.printIndent().println( "xpCH = xpOut.getXpContentHandler();");

            processorResultContent = new ProcessorResultContent(getTranslaterContext());
            if (null != sb) {
                processorResultContent.characters(sb.toString().toCharArray(), 0, sb.length());
                sb = null;
            }
        }
        return processorResultContent.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
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

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (valueAttributeMode) {
            throw new SAXParseException("Text content not allowed here when @value is present on <xp:attribute>;", getContext().getLocator());
        } else {
            if (optimizedMode) {
                if (null == sb) {
                    sb = new StringBuffer();
                }
                sb.append(ch, start, length);
            } else {
                processorResultContent.characters(ch, start, length);
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (valueAttributeMode) {
            return;
        }
        CodeWriter out = getTranslaterContext().getCodeWriter();
        String codeValue;

        if (optimizedMode) {
            if (sb != null) {
                String value = sb.toString();
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
                sb = null;
            } else {
                codeValue = Util.escapeStringQuoted("");
            }
            out.printIndent().println(
                  "xpCH.addAttribute("
                +       codeURI
                + "," + codeName
                + "," + codeValue
                + ");"
            );
        } else {
            processorResultContent.flushCharacters();
            codeValue = "((org.anodyneos.xp.util.TextContentHandler) xpCH.getWrappedContentHandler()).getText()";
            if (trim) {
                codeValue = codeValue + ".trim()";
            }
            out.printIndent().println(
                  savedXPOutVariable + ".addAttribute("
                +       codeURI
                + "," + codeName
                + "," + codeValue
                + ");"
            );

            out.printIndent().println( "xpOut = " + savedXPOutVariable + ";");
            out.printIndent().println( "xpCH = xpOut.getXpContentHandler();");
            out.printIndent().println( savedXPOutVariable + " = null;");
        }
    }
}
