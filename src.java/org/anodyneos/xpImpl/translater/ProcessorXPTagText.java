package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * TextTag supports outputting text without whitespace handling.
 *
 * &lt;xp:text>some text&lt;/xp:text>

 * &lt;xp:text> &lt;/xp:text>
 *
 * @author jvas
 */
public class ProcessorXPTagText extends HelperProcessorNonResultContent {

    private StringBuffer sb;

    public ProcessorXPTagText(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return super.getProcessorFor(uri, localName, qName);
    }

    public void startElementNonResultContent(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
    }

    public void endElementNonResultContent(String uri, String localName,
            String name) throws SAXException {
        flushCharacters();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (null == sb) {
            sb = new StringBuffer();
        }
        sb.append(ch, start, length);
    }

    public void flushCharacters() throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        if (sb != null) {
            String s = sb.toString();
            if (s.length() > 0) { // don't output if empty
                Util.outputCharactersCode(s, out);
            }
            sb = null;
        }
    }
}
