package org.anodyneos.xpImpl.translater;


import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.xml.sax.SAXException;

class TranslaterProcessor extends ElementProcessor {

    public static final String URI_XP = "http://www.anodyneos.org/xmlns/xp";
    public static final String URI_XP_TAG = "http://www.anodyneos.org/xmlns/xptag";
    public static final String URI_NAMESPACES = "http://www.w3.org/2000/xmlns/";

    private TranslaterContext ctx;

    public TranslaterProcessor(TranslaterContext ctx) {
        super(ctx);
        this.ctx = ctx;
    }

    protected TranslaterContext getTranslaterContext() {
        return ctx;
    }

    public void startPrefixMapping(java.lang.String prefix, java.lang.String uri) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println(
              "xpCH.startPrefixMapping("
            + "\""   + prefix + "\""
            + ",\""  + uri + "\""
            + ");"
        );
    }

    public void endPrefixMapping(java.lang.String prefix) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println(
              "xpCH.endPrefixMapping("
            + "\""   + prefix + "\""
            + ");"
        );
    }

}
