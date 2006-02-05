package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * RemoveTag supports clearing a value of a scripting variable
 *
 * <xp:remove @var [@scope]/>
 *
 * @scope and @var are NOT dynamic
 *
 * @author jvas
 */
public class ProcessorXPTagRemove extends TranslaterProcessorNonResultContent {

    public static final String A_VAR = "var";
    public static final String A_SCOPE = "scope";

    public ProcessorXPTagRemove(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return super.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        String var = attributes.getValue(A_VAR);
        String scope = attributes.getValue(A_SCOPE);

        if(null == var) {
            throw new SAXParseException("@var is required for xp:remove.", getContext().getLocator());
        }

        String codeVar = null == var ? null : Util.escapeStringQuotedEL(var);
        String codeScope = null == scope ? null : Util.escapeStringQuotedEL(scope);

        out.printIndent().println(
              "org.anodyneos.xpImpl.runtime.XPTagRemoveHelper.remove(xpContext"
            + ", " + codeVar
            + ", " + codeScope
            + ");"
        );
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        throw new SAXParseException("characters not yet allowed here.", getContext().getLocator());
    }

}
