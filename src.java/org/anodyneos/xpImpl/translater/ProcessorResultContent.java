package org.anodyneos.xpImpl.translater;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * ProcessorResultContent handles all output elements (elements that do not match a known namespace)
 * and text that appears within those elements.
 *
 * @author jvas
 */
public class ProcessorResultContent extends TranslaterProcessor {

    private StringBuffer sb;

    public static final String E_ATTRIBUTE = "attribute";

    /**
     * We used to have XpContentHandler take care of adding xp:attributes, but
     * now we will handle them directly. attributeProcessors will be populated with
     * all xp:attributes wich will be written out along with the startElement
     * once they are all read in (before text, new elements, etc).  After endElement, we
     * will call endPrefixMapping as necessary.
     *
     * TODO: !!! This was a mistake, need to have XpContentHandler handle attributes so
     * they can be wrapped in if statements.  Probably need an element stack in XpContentHandler
     * in order to save calls for endPrefixMapping.
     */
    private List attributeProcessors = new ArrayList();
    private String elURI;
    private String elLocalName;
    private String elQName;
    private Attributes elAttributes;
    // track when we need to print startElement code
    private boolean startElementCalled = false;
    private boolean startElementPrinted = false;

    public ProcessorResultContent(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        if (! (URI_XP.equals(uri) && E_ATTRIBUTE.equals(localName))) {
            // only print this element if next element is NOT an xp:attribute
            printStartElement();
        }
        // looks like a new element is comming, so flush.
        flushCharacters();
        if (URI_XP.equals(uri)) {
            if (E_ATTRIBUTE.equals(localName)) {
                if (startElementPrinted) {
                    // throw exception
                    return super.getProcessorFor(uri, localName, qName);
                } else {
                    // for output content, not attributes for action tags
                    ProcessorAttribute pa = new ProcessorAttribute(getTranslaterContext());
                    attributeProcessors.add(pa);
                    return pa;
                }
            } else {
                // throw exception
                return super.getProcessorFor(uri, localName, qName);
            }
        } else if (null != getTranslaterContext().getTagLibraryRegistry().getTagLibraryInfo(uri)) {
            return new ProcessorTag(getTranslaterContext());
        } else {
            // handle more result tree content
            return new ProcessorResultContent(getTranslaterContext());
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        startElementCalled = true;
        elURI = uri;
        elLocalName = localName;
        elQName = qName;
        // must make copy of attributes; SAX reuses the one passed in
        elAttributes = new AttributesImpl(attributes);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (! startElementPrinted) {
            // if non-whitespace characters, output startElement
            for (int i = start; i < start + length; i++) {
                if(ch[i] != ' ' && ch[i] != '\r' && ch[i] != '\n') {
                    printStartElement();
                    break;
                }
            }
        }
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
        printStartElement();
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

        // print "extra" endPrefixMappings
        Iterator it = attributeProcessors.iterator();
        while(it.hasNext()) {
            ProcessorAttribute pa = (ProcessorAttribute) it.next();
            if(pa.declareNamespace) {
                out.printIndent().println(
                      "xpCH.endPrefixMapping("
                    +       Util.escapeStringQuoted(pa.attPrefix)
                    + ");"
                );
            }
        }
    }

    private void printStartElement() throws SAXException {
        if (startElementCalled && ! startElementPrinted) {
            // clear out saved whitespace characters
            sb = null;
            startElementPrinted = true;

            CodeWriter out = getTranslaterContext().getCodeWriter();

            // print "extra" beginPrefixMappings
            Iterator it = attributeProcessors.iterator();
            while(it.hasNext()) {
                ProcessorAttribute pa = (ProcessorAttribute) it.next();
                if(pa.declareNamespace) {
                    out.printIndent().println(
                          "xpCH.startPrefixMapping("
                        +       Util.escapeStringQuoted(pa.attPrefix)
                        + "," + Util.escapeStringQuoted(pa.attURI)
                        + ");"
                    );
                }
            }

            // start element
            out.printIndent().println(
                  "xpCH.startElement("
                +        Util.escapeStringQuoted(elURI)
                + ", " + Util.escapeStringQuoted(elLocalName)
                + ", " + Util.escapeStringQuoted(elQName)
                + ", null);"
            );

            // set attributes
            for (int i = 0; i < elAttributes.getLength(); i++) {
                String value = elAttributes.getValue(i);
                String codeValue;
                if(Util.hasEL(value)) {
                    // EL expression may exist.  Process all unescaped expressions, concatinate, etc...
                    codeValue = Util.elExpressionCode(value, "String");
                } else {
                    codeValue = Util.escapeStringQuotedEL(value);
                }
                out.printIndent().println(
                      "xpCH.addAttribute("
                    +        Util.escapeStringQuoted(elAttributes.getURI(i))
                    + ", " + Util.escapeStringQuoted(elAttributes.getLocalName(i))
                    + ", " + Util.escapeStringQuoted(elAttributes.getQName(i))
                    + ", " + Util.escapeStringQuoted(elAttributes.getType(i))
                    + ", " + codeValue
                    + ");"
                );
            }

            // set xp:attributes
            it = attributeProcessors.iterator();
            while(it.hasNext()) {
                ProcessorAttribute pa = (ProcessorAttribute) it.next();
                if (pa.declareNamespace) {
                    out.printIndent().println(
                          "xpCH.addAttribute("
                        +        Util.escapeStringQuoted("")
                        + ", " + Util.escapeStringQuoted("")
                        + ", " + Util.escapeStringQuoted("xmlns:" + pa.attPrefix)
                        + ", " + Util.escapeStringQuoted("CDATA")
                        + ", " + Util.escapeStringQuoted(pa.attURI)
                        + ");"
                    );
                }
                out.printIndent().println(
                      "xpCH.addAttribute("
                    +        Util.escapeStringQuoted(pa.attURI)
                    + ", " + Util.escapeStringQuoted(pa.attLocalName)
                    + ", " + Util.escapeStringQuoted(pa.attQName)
                    + ", " + Util.escapeStringQuoted("CDATA")
                    + ", " + pa.attCodeValue
                    + ");"
                );
            }
        }
    }

}
