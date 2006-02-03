package org.anodyneos.xpImpl.translater;

import java.util.Iterator;
import java.util.Map;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * ProcessorResultContent handles all output elements (elements that do not match a known namespace)
 * and text that appears within those elements.
 *
 * @author jvas
 */
public class ProcessorResultContent extends TranslaterProcessor {

    private StringBuffer sb;

    public static final String E_ATTRIBUTE = "attribute";
    public static final String E_IF = "if";
    public static final String E_CHOOSE = "choose";
    public static final String E_SET = "set";
    public static final String E_REMOVE = "remove";
    public static final String E_NEW_BEAN = "newBean";
    public static final String E_INCLUDE = "include";

    public ProcessorResultContent(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        // looks like a new element is comming, so flush characters.
        flushCharacters();
        if (URI_XP.equals(uri)) {
            if (E_ATTRIBUTE.equals(localName)) {
                // for output content, not attributes for action tags
                return new ProcessorAttribute(getTranslaterContext());
            } else if (E_IF.equals(localName)) {
                return new ProcessorXPTagIf(getTranslaterContext());
            } else if (E_CHOOSE.equals(localName)) {
                return new ProcessorXPTagChoose(getTranslaterContext());
            } else if (E_SET.equals(localName)) {
                return new ProcessorXPTagSet(getTranslaterContext());
            } else if (E_REMOVE.equals(localName)) {
                return new ProcessorXPTagRemove(getTranslaterContext());
            } else if (E_NEW_BEAN.equals(localName)) {
                return new ProcessorXPTagNewBean(getTranslaterContext());
            } else if (E_INCLUDE.equals(localName)) {
                return new ProcessorXPTagInclude(getTranslaterContext());
            } else {
                return super.getProcessorFor(uri, localName, qName);
            }
        } else if (null != getTranslaterContext().getTagLibraryRegistry().getTagLibraryInfo(uri)) {
            return new ProcessorTag(getTranslaterContext());
        } else {
            // handle more result tree content with this
            return this;
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        // buffered startPrefixMappings may exist, lets output them
        Map prefixBuffer = getTranslaterContext().getBufferedStartPrefixMappings();
        Iterator it = prefixBuffer.keySet().iterator();
        while (it.hasNext()) {
            String prefix = (String) it.next();
            String tmpUri = (String) prefixBuffer.get(prefix);
            out.printIndent().println(
                    "xpCH.startPrefixMapping("
                  + "\""   + prefix + "\""
                  + ",\""  + tmpUri + "\""
                  + ");"
            );
        }
        getTranslaterContext().clearBufferedStartPrefixMappings();

        // start element
        out.printIndent().println(
              "xpCH.startElement("
            +        Util.escapeStringQuoted(uri)
            + ", " + Util.escapeStringQuoted(localName)
            + ", " + Util.escapeStringQuoted(qName)
            + ", null);"
        );

        // set attributes
        for (int i = 0; i < attributes.getLength(); i++) {
            String value = attributes.getValue(i);
            String codeValue;
            if(Util.hasEL(value)) {
                // EL expression may exist.  Process all unescaped expressions, concatinate, etc...
                codeValue = Util.elExpressionCode(value, "String");
            } else {
                codeValue = Util.escapeStringQuotedEL(attributes.getValue(i));
            }
            out.printIndent().println(
                  "xpCH.addAttribute("
                +        Util.escapeStringQuoted(attributes.getURI(i))
                + ", " + Util.escapeStringQuoted(attributes.getQName(i))
                + ", " + codeValue
                + ");"
            );
        }
    }

    public void characters(char[] ch, int start, int length) {
        if (null == sb) {
            sb = new StringBuffer();
        }
        sb.append(ch, start, length);
    }

    public void flushCharacters() throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        if (sb != null) {
            String s = sb.toString();
            // TODO: what about strip-space? Is this what we want? Configurable?
            s = s.trim();
            if (!"".equals(s)) { // don't output if only whitespace
                Util.outputCharactersCode(s, out);
            }
            sb = null;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        // end element
        flushCharacters();
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println(
                "xpCH.endElement("
              +        Util.escapeStringQuoted(uri)
              + ", " + Util.escapeStringQuoted(localName)
              + ", " + Util.escapeStringQuoted(qName)
              + ");"
        );
    }

}
