package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * SetTag supports setting a value of a scripting variable or a property of a
 * target that may be a java bean or Map.
 *
 * <xp:set @value @var [@scope]/>
 *
 * <xp:set @var [@scope]>bodyContent </xp:set> (Not yet supported)
 *
 * <xp:set @value @target @property/>
 *
 * <xp:set @target @property>bodyContent </xp:set> (Not yet supported)
 *
 * @scope and @var are NOT dynamic, others are.
 *
 * @author jvas
 */
public class ProcessorXPTagSet extends TranslaterProcessorNonResultContent {

    public static final String A_VALUE = "value";
    public static final String A_TARGET = "target";
    public static final String A_PROPERTY = "property";
    public static final String A_VAR = "var";
    public static final String A_SCOPE = "scope";

    public ProcessorXPTagSet(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return super.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        String value = attributes.getValue(A_VALUE);
        String target = attributes.getValue(A_TARGET);
        String property = attributes.getValue(A_PROPERTY);
        String var = attributes.getValue(A_VAR);
        String scope = attributes.getValue(A_SCOPE);

        if(null == value) {
            throw new SAXParseException("@value is currently required.", getContext().getLocator());
        }
        if((null == var && null == target) || (null != var && null != target)) {
            throw new SAXParseException("Either @var or @target is required, but not both.", getContext().getLocator());
        }
        if(null != target && (null == property || null != scope)) {
            throw new SAXParseException("When @target is specified, @property must exist, @scope must not exist.", getContext().getLocator());
        }

        String codeVar = null == var ? null : Util.escapeStringQuotedEL(var);
        String codeScope = null == scope ? null : Util.escapeStringQuotedEL(scope);
        String codeTarget = null == target ? null : Util.elExpressionCode(target, "Object");
        String codeProperty = null == property ? null : Util.elExpressionCode(property, "String");
        String codeValue = null == value ? null : Util.elExpressionCode(value, "Object");

        out.printIndent().println(
              "org.anodyneos.xpImpl.runtime.XPTagSetHelper.set(xpContext"
            + ", " + codeVar
            + ", " + codeScope
            + ", " + codeTarget
            + ", " + codeProperty
            + ", " + codeValue
            + ");"
        );
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        throw new SAXParseException("characters not yet allowed here.", getContext().getLocator());
    }

}
