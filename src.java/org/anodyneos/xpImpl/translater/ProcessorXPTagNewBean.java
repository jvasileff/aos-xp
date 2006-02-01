package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * NewBean  creates a new object and stores it into the scope (default scope is page) with the name @var.
 *
 * @className, @scope and @var are NOT dynamic.  Reflection is used to instantiate bean - this
 * will be necessary for when we start to run into Classloader problems.
 *
 * @author jvas
 */
public class ProcessorXPTagNewBean extends HelperProcessorNonResultContent {

    public static final String A_VAR = "var";
    public static final String A_SCOPE = "scope";
    public static final String A_CLASS_NAME = "class";

    public ProcessorXPTagNewBean(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return super.getProcessorFor(uri, localName, qName);
    }

    public void startElementNonResultContent(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        String var = attributes.getValue(A_VAR);
        String scope = attributes.getValue(A_SCOPE);
        String className = attributes.getValue(A_CLASS_NAME);

        if(null == var) {
            throw new SAXParseException("@var is required.", getContext().getLocator());
        }
        if(null == className) {
            throw new SAXParseException("@className is required.", getContext().getLocator());
        }

        String codeVar = null == var ? null : Util.escapeStringQuotedEL(var);
        String codeScope = null == scope ? null : Util.escapeStringQuotedEL(scope);
        String codeClassName = null == className ? null : Util.escapeStringQuotedEL(className);

        out.printIndent().println(
              "org.anodyneos.xpImpl.runtime.XPTagNewBeanHelper.newBean(xpContext"
            + ", " + codeVar
            + ", " + codeScope
            + ", " + codeClassName
            + ");"
        );
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        throw new SAXParseException("characters not allowed here.", getContext().getLocator());
    }

}
