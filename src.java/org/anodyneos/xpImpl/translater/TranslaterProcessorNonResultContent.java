package org.anodyneos.xpImpl.translater;

import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.SAXException;


/**
 * @author jvas
 */
public class TranslaterProcessorNonResultContent extends TranslaterProcessor {

    public TranslaterProcessorNonResultContent(TranslaterContext ctx) {
        super(ctx);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        out.printIndent().println(
                "xpCH.pushPhantomPrefixMapping("
              +       Util.escapeStringQuoted(prefix)
              + "," + Util.escapeStringQuoted(uri)
              + ");");
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        out.printIndent().println("xpCH.popPhantomPrefixMapping();");
    }

}
