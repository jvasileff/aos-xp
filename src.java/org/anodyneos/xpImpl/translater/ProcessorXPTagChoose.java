package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * ChooseTag similar to xsl.
 *
 * <code>
 *      &lt;xp:choose>
 *          &lt;xp:when @test>fragment</xp:when>
 *          &lt;xp:when @test>fragment</xp:when>
 *          &lt;xp:otherwise>fragment</xp:otherwise>
 *      &lt;/xp:choose>
 * </code>
 *
 * @author jvas
 */
public class ProcessorXPTagChoose extends TranslaterProcessor {

    public static final String E_WHEN = "when";
    public static final String E_OTHERWISE = "otherwise";

    private boolean nextIsFirst = true;
    private boolean otherwiseCalled = false;

    public ProcessorXPTagChoose(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        if (URI_XP.equals(uri)) {
            if (E_WHEN.equals(localName)) {
                if (otherwiseCalled) {
                    throw new SAXParseException("<xp:when> cannot follow <xp:otherwise>.", getContext().getLocator());
                }
                ElementProcessor proc = new ProcessorXPTagWhen(getTranslaterContext(), false, nextIsFirst);
                nextIsFirst = false;
                return proc;
            } else if (E_OTHERWISE.equals(localName)) {
                ElementProcessor proc = new ProcessorXPTagWhen(getTranslaterContext(), true, nextIsFirst);
                nextIsFirst = false;
                otherwiseCalled = true;
                return proc;
            } else {
                return super.getProcessorFor(uri, localName, qName);
            }
        } else {
            return super.getProcessorFor(uri, localName, qName);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if(nextIsFirst) {
            throw new SAXParseException("No <xp:when> found.", getContext().getLocator());
        }

        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.endBlock();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
            case ' ':
            case '\r':
            case '\n':
            case '\t':
            case '\f':
                break;
            default:
                System.out.println("Character found: " + ch[i]);
                throw new SAXParseException("characters not allowed here.", getContext().getLocator());
            }
        }
    }

}
